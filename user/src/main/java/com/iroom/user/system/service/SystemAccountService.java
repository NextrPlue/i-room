package com.iroom.user.system.service;

import com.iroom.user.common.jwt.JwtTokenProvider;
import com.iroom.user.system.dto.request.SystemAuthRequest;
import com.iroom.user.system.dto.response.SystemAuthResponse;
import com.iroom.user.system.entity.SystemAccount;
import com.iroom.user.system.repository.SystemAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemAccountService {

    private final SystemAccountRepository systemAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public SystemAuthResponse authenticate(SystemAuthRequest request) {
        SystemAccount system =  systemAccountRepository.findByApiKey(request.apiKey())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 API Key 입니다."));

        return new SystemAuthResponse(jwtTokenProvider.createSystemToken(system));
    }
}
