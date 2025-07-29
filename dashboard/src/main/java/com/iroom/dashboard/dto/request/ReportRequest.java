package com.iroom.dashboard.dto.request;

public record ReportRequest(
	int missingPpeCnt,
	int dangerZoneAccessCnt,
	int healthAlertCnt
) {
}
