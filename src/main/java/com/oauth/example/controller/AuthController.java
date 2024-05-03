package com.oauth.example.controller;

import com.oauth.example.domain.dto.*;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.model.ApiResponse;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.UserService;
import com.oauth.example.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

@Tag(name = "Authentication")
@RestController
@RequestMapping(path = "/v1/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid @NonNull LoginRequest request) {
        return ResponseEntity.ok().body(authService.authenticate(request));
    }

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SignupResponse> register(@RequestBody @Valid SignUpRequest request) {
        var response = authService.register(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void triggerForgotPasswordFlow(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) throws NotFoundException, IOException {
        authService.triggerForgotPasswordFlow(forgotPasswordRequest.getEmail());
    }

    @PostMapping("recover-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recoverPassword(@RequestBody @Valid RecoverPasswordRequest recoverPasswordRequest) throws NotFoundException {
        authService.recoverPassword(recoverPasswordRequest);
    }

    @PostMapping("accept-user-invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptUserInvite(@RequestBody @Valid AcceptUserInviteDto acceptUserInviteDto) throws NotFoundException {
        userService.acceptUserInvite(acceptUserInviteDto);
    }

    @GetMapping("get-tenants")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Set<AssignableTenant>>> getTenants(@AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseBuilder<Set<AssignableTenant>>().body(userService.getTenants(principal.getId())).build();
    }
}

