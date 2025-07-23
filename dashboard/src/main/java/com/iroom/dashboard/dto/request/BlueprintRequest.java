package com.iroom.dashboard.dto.request;

public record BlueprintRequest (
        String blueprintUrl,
        Integer floor,
        Double width,
        Double height
){
}
