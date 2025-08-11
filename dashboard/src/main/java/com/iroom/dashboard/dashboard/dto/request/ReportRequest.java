package com.iroom.dashboard.dashboard.dto.request;

public record ReportRequest(
	int missingPpeCnt,
	int dangerZoneAccessCnt,
	int healthAlertCnt
) {
}
