package com.iroom.modulecommon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemRole {
    ENTRANCE_SYSTEM("ROLE_ENTRANCE_SYSTEM"),
    WORKER_SYSTEM("ROLE_WORKER_SYSTEM"),
    EQUIPMENT_SYSTEM("ROLE_EQUIPMENT_SYSTEM");

    private final String key;
}
