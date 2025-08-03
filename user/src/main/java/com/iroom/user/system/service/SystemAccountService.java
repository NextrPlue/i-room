package com.iroom.user.system.service;

import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_INVALID_API_KEY));

        return new SystemAuthResponse(jwtTokenProvider.createSystemToken(system));
    }
}
