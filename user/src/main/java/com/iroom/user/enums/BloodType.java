package com.iroom.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BloodType {
    A("A형"),
    B("B형"),
    AB("AB형"),
    O("O형");

    private final String description;
}