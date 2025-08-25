package com.iroom.dashboard.danger.util;

public class DistanceUtil {

	private static final double EARTH_RADIUS = 6371000;

	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double rLat1 = Math.toRadians(lat1);
		double rLat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
			+ Math.cos(rLat1) * Math.cos(rLat2)
			* Math.sin(dLon / 2) * Math.sin(dLon / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}

	/**
	 * GPS 좌표가 사각형 위험구역 내부에 있는지 판정
	 * @param workerLat 작업자 위도
	 * @param workerLon 작업자 경도
	 * @param centerLat 위험구역 중심 위도
	 * @param centerLon 위험구역 중심 경도
	 * @param width 위험구역 가로 크기 (미터)
	 * @param height 위험구역 세로 크기 (미터)
	 * @return 위험구역 내부에 있으면 true, 외부에 있으면 false
	 */
	public static boolean isPointInsideRectangleArea(double workerLat, double workerLon,
		double centerLat, double centerLon, double width, double height) {

		// 위험구역 중심에서 각 방향으로의 반경 (미터)
		double halfWidth = width / 2.0;
		double halfHeight = height / 2.0;

		// 위도 1도당 거리 (약 111,000m)
		double latDegreeDistance = 111000.0;
		// 경도 1도당 거리 (위도에 따라 변함)
		double lonDegreeDistance = 111000.0 * Math.cos(Math.toRadians(centerLat));

		// 위험구역 경계 계산 (GPS 좌표)
		double northBoundary = centerLat + (halfHeight / latDegreeDistance);
		double southBoundary = centerLat - (halfHeight / latDegreeDistance);
		double eastBoundary = centerLon + (halfWidth / lonDegreeDistance);
		double westBoundary = centerLon - (halfWidth / lonDegreeDistance);

		// 작업자가 사각형 영역 내부에 있는지 판정
		boolean insideLatRange = (workerLat >= southBoundary && workerLat <= northBoundary);
		boolean insideLonRange = (workerLon >= westBoundary && workerLon <= eastBoundary);

		return insideLatRange && insideLonRange;
	}
}
