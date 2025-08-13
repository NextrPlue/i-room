package com.iroom.sensor.util;


public class GpsFilter {

	// 칼만 필터 변수
	private double latEstimate;
	private double lonEstimate;
	private double latError = 1.0;
	private double lonError = 1.0;
	private final double processNoise = 0.00001;  // 프로세스 노이즈
	private boolean isInitialized = false;

	public GpsFilter() {}

	/**
	 * GPS 좌표 보정 (정지+이동 모두 칼만 필터)
	 * @param lat 위도
	 * @param lon 경도
	 * @param speed 현재 속도(m/s)
	 * @return 보정된 [위도, 경도]
	 */
	public double[] filter(double lat, double lon, double speed) {
		double measurementNoise;
		if (speed < 0.5) {
			// 정지 상태 → GPS 값을 더 신뢰
			measurementNoise = 0.00001;
		} else {
			// 이동 상태 → 부드러운 변화 반영
			measurementNoise = 0.0001;
		}
		return kalmanFilter(lat, lon, measurementNoise);
	}

	// 1차 칼만 필터 (위도/경도 각각 독립)
	private double[] kalmanFilter(double lat, double lon, double measurementNoise) {
		// 초기화 단계 — 첫 GPS값 그대로 사용
		if (!isInitialized) {
			latEstimate = lat;
			lonEstimate = lon;
			isInitialized = true;
			return new double[]{latEstimate, lonEstimate};
		}

		// 예측 단계
		latError += processNoise;
		lonError += processNoise;

		// 갱신 단계 (위도)
		double kLat = latError / (latError + measurementNoise);
		latEstimate = latEstimate + kLat * (lat - latEstimate);
		latError = (1 - kLat) * latError;

		// 갱신 단계 (경도)
		double kLon = lonError / (lonError + measurementNoise);
		lonEstimate = lonEstimate + kLon * (lon - lonEstimate);
		lonError = (1 - kLon) * lonError;

		return new double[]{latEstimate, lonEstimate};
	}
}
