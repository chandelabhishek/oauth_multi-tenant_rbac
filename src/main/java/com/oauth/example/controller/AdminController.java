package com.oauth.example.controller;

import com.oauth.example.domain.dto.*;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.model.ApiResponse;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.TenantService;
import com.oauth.example.service.UserService;
import com.oauth.example.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "admin")
@RestController
@RequestMapping(path = "/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TenantService tenantService;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok().body(authService.authenticateAdmin(request));
    }

    @PostMapping("/create-tenant")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<TenantDto>> createTenant(@RequestBody @Valid Tenant tenant) {
        return new ResponseBuilder<TenantDto>()
                .body(tenantService.createTenant(tenant))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/tenants/{tenantId}/add-user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void addTenantUser(@RequestBody @Valid UserDto userDto, @PathVariable UUID tenantId) throws IOException, NotFoundException {
        tenantService.addTenantUser(userDto, tenantId);
    }

    @PostMapping("/create-user")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody @Valid SignUpRequest request) {
        userService.save(request);
    }

    @PostMapping("/tenant/{tenantId}/add-user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void addTenantUser(@RequestParam UUID tenantId, @RequestParam UUID userId) throws NotFoundException {
        tenantService.addTenantUser(tenantId, userId);
    }
}
