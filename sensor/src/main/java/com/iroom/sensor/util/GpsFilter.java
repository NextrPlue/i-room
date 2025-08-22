package com.iroom.sensor.util;

public class GpsFilter {

	// --- 필터 내부 상태 (지역좌표: meters, ENU 근사) ---
	private boolean isInitialized = false;
	private double refLatDeg;   // 지역좌표 원점 위도
	private double refLonDeg;   // 지역좌표 원점 경도

	private double xEst;        // 동(East) 방향 추정 (m)
	private double yEst;        // 북(North) 방향 추정 (m)
	private double xErr = 25.0; // 추정 오차 분산(초기값은 다소 크게) m^2
	private double yErr = 25.0; // 추정 오차 분산 m^2

	// --- 노이즈/임계 상수 ---
	// 프로세스 노이즈(이동 모델 불확실성; 호출 간격을 몰라 보수적으로 설정)
	private final double qStationary = 1.0;   // m^2 (정지시)
	private final double qMoving = 9.0;   // m^2 (이동시)

	// Outlier 판단 임계치: 잔차(m)가 이 값을 넘으면 업데이트 스킵
	private final double baseOutlierThreshM = 25.0;          // 최소 25 m
	private final double dynOutlierSigmaMul = 5.0;           // 5 * sqrt(R)
	private final double outlierInflation = 100.0;         // 스킵 시 분산 팽창량(m^2)

	// 지구 반경(단순화)
	private static final double R_EARTH = 6378137.0;         // meters

	public GpsFilter() {
	}

	/**
	 * 기존 인터페이스(HDOP/accuracy 없음). 내부에서 보수적 기본값으로 동적 보정.
	 */
	public double[] filter(double latDeg, double lonDeg, double speedMps) {
		return filter(latDeg, lonDeg, speedMps, -1.0, -1.0);
	}

	/**
	 * 개선 인터페이스: HDOP 또는 accuracyMeters가 있으면 measurement noise를 동적으로 조정
	 * @param latDeg 위도(deg)
	 * @param lonDeg 경도(deg)
	 * @param speedMps 현재 속도(m/s)
	 * @param hdop 수신기 HDOP (없으면 음수)
	 * @param accuracyMeters 수신기 제공 정확도(m, CEP68 등 기기값) (없으면 음수)
	 * @return 보정된 [lat, lon]
	 */
	public double[] filter(double latDeg, double lonDeg, double speedMps, double hdop, double accuracyMeters) {
		if (!isInitialized) {
			// 초기화: 첫 좌표를 지역좌표 원점으로
			this.refLatDeg = latDeg;
			this.refLonDeg = lonDeg;
			this.xEst = 0.0;
			this.yEst = 0.0;
			this.isInitialized = true;
			return new double[] {latDeg, lonDeg};
		}

		// --- 1) 측정값을 지역좌표(m)로 변환 ---
		double[] measXY = toLocalMeters(latDeg, lonDeg, refLatDeg, refLonDeg);
		double zx = measXY[0];
		double zy = measXY[1];

		// --- 2) 프로세스 노이즈 Q 선택(정지/이동) ---
		double q = (speedMps < 0.5) ? qStationary : qMoving;

		// --- 3) 측정 노이즈 R 결정 (m^2) ---
		double rMeasVar = estimateMeasurementVariance(hdop, accuracyMeters, speedMps);

		// --- 4) 예측단계: 상태전이 없이 분산만 증가 ---
		xErr += q;
		yErr += q;

		// --- 5) Outlier 체크 (innovation norm) ---
		double rx = zx - xEst;
		double ry = zy - yEst;
		double residual = Math.hypot(rx, ry); // m

		double dynThresh = Math.max(baseOutlierThreshM, dynOutlierSigmaMul * Math.sqrt(rMeasVar));
		if (residual > dynThresh) {
			// 측정값이 비정상적으로 멀다 → 이번 업데이트는 스킵(soft reject)
			// 대신 분산을 조금 팽창시켜 추정이 뒤늦게 따라올 수 있게 함
			xErr += outlierInflation;
			yErr += outlierInflation;
			// 추정치 유지 후 반환
			return toLatLon(xEst, yEst, refLatDeg, refLonDeg);
		}

		// --- 6) 갱신단계 (축 별 스칼라 칼만 업데이트) ---
		double kx = xErr / (xErr + rMeasVar);
		double ky = yErr / (yErr + rMeasVar);

		xEst = xEst + kx * (zx - xEst);
		yEst = yEst + ky * (zy - yEst);

		xErr = (1.0 - kx) * xErr;
		yErr = (1.0 - ky) * yErr;

		// --- 7) 지역좌표 → 위경도 변환 ---
		return toLatLon(xEst, yEst, refLatDeg, refLonDeg);
	}

	/**
	 * HDOP / accuracy / 속도에 기반해 측정 분산 R(m^2)을 추정
	 * 우선순위: accuracyMeters > hdop > 기본값(정지/이동)
	 */
	private double estimateMeasurementVariance(double hdop, double accuracyMeters, double speedMps) {
		// 1) 기기가 제공하는 accuracy가 가장 신뢰도 높음
		if (accuracyMeters > 0) {
			// 분산 = sigma^2, 여기서 sigma를 accuracyMeters로 간주
			double sigma = accuracyMeters;
			return sigma * sigma;
		}

		// 2) HDOP 기반 추정
		if (hdop > 0) {
			// 일반적 근사: sigma ≈ (hdop * 5 m) (환경 따라 조정)
			double sigma = Math.max(5.0, 5.0 * hdop);
			return sigma * sigma;
		}

		// 3) 아무 정보도 없으면 보수적 기본값
		if (speedMps < 0.5) {
			// 정지 시 GPS를 비교적 신뢰
			double sigma = 3.0; // 3 m 1-sigma
			return sigma * sigma;
		} else {
			// 이동 시 값이 요동칠 수 있어 더 크게
			double sigma = 8.0; // 8 m 1-sigma
			return sigma * sigma;
		}
	}

	// --- 위경도 <-> 지역좌표(근사 ENU, meters) 변환 유틸 ---

	/**
	 * 기준(refLat, refLon) 주변에서의 간단한 equirectangular 근사.
	 * 동(E): Δlon * cos(refLat) * R
	 * 북(N): Δlat * R
	 */
	private static double[] toLocalMeters(double latDeg, double lonDeg, double refLatDeg, double refLonDeg) {
		double dLat = Math.toRadians(latDeg - refLatDeg);
		double dLon = Math.toRadians(lonDeg - refLonDeg);
		double meanLat = Math.toRadians(refLatDeg);
		double x = dLon * Math.cos(meanLat) * R_EARTH; // East
		double y = dLat * R_EARTH;                     // North
		return new double[] {x, y};
	}

	private static double[] toLatLon(double x, double y, double refLatDeg, double refLonDeg) {
		double dLat = y / R_EARTH;                           // radians
		double dLon = x / (R_EARTH * Math.cos(Math.toRadians(refLatDeg)));
		double lat = refLatDeg + Math.toDegrees(dLat);
		double lon = refLonDeg + Math.toDegrees(dLon);
		return new double[] {lat, lon};
	}
}
