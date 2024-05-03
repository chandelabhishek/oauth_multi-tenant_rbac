package com.oauth.example.controller;

import com.oauth.example.domain.dto.AssignTenantDto;
import com.oauth.example.domain.dto.AuthResponse;
import com.oauth.example.domain.dto.PasswordChangeRequest;
import com.oauth.example.domain.dto.UserMeDto;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.model.ApiResponse;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.UserService;
import com.oauth.example.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user/")
@Tag(name = "User")
@RequiredArgsConstructor
public class UserController {
    private final UserService userServiceImpl;
    private final AuthService authService;

    @GetMapping("me")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<UserMeDto>> me(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return new ResponseBuilder<UserMeDto>().body(userServiceImpl.me(userPrincipal)).build();
    }

    @PostMapping("change-password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        userServiceImpl.changePassword(userPrincipal, passwordChangeRequest);
    }

    @PostMapping("assign-tenant")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<AuthResponse>> assignTenant(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid AssignTenantDto assignTenantDto) throws NotFoundException {
        return new ResponseBuilder<AuthResponse>().body(
                authService.assignTenantAndGenerateToken(
                        userPrincipal.getUser(),
                        assignTenantDto.getTenantId()
                )
        ).build();
    }
}
