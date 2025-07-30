package com.iroom.user.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemRole {
    SYSTEM("ROLE_SYSTEM");

    private final String key;
}
