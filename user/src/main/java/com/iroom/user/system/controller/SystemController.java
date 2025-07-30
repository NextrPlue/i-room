package com.iroom.user.system.controller;

import com.iroom.user.system.dto.request.SystemAuthRequest;
import com.iroom.user.system.dto.response.SystemAuthResponse;
import com.iroom.user.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/systems")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @PostMapping("/authenticate")
    public ResponseEntity<SystemAuthResponse> authenticate(@RequestBody SystemAuthRequest request) {
        SystemAuthResponse response = systemService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
