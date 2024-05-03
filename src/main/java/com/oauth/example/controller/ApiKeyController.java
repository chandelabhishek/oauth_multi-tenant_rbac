package com.oauth.example.controller;

import com.oauth.example.domain.dto.ApiKeysDto;
import com.oauth.example.domain.model.ApiResponse;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.ApiKeyService;
import com.oauth.example.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Api Keys")
@RestController
@RequestMapping("/v1/api-keys")
public class ApiKeyController {
    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ApiKeysDto>> createApiKeys(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return new ResponseBuilder<ApiKeysDto>()
                .body(apiKeyService.createApiKeys(userPrincipal.getUser()))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }
}
