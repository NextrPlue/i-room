package com.iroom.management.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkerRole {
    WORKER("ROLE_WORKER");

    private final String key;
}
