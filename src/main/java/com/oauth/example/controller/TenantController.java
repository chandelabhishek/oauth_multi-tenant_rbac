package com.oauth.example.controller;

import com.oauth.example.domain.dto.AddExistingUserDto;
import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.dto.TenantUserResponseDto;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.enums.TenantType;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.model.ApiResponse;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.TenantService;
import com.oauth.example.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Tenant")
@RestController
@RequestMapping("/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Tenant>> getTenant(@AuthenticationPrincipal UserPrincipal userPrincipal) throws NotFoundException {
        return new ResponseBuilder<Tenant>()
                .body(tenantService.getTenant(userPrincipal))
                .build();
    }

    @PutMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<TenantDto>> updateTenant(@RequestBody @Valid TenantDto tenantDTO) throws NotFoundException {
        return new ResponseBuilder<TenantDto>()
                .body(tenantService.update(tenantDTO))
                .build();
    }

    @PostMapping("/add-user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void addTenantUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody UserDto userDto) throws Exception {
        tenantService.addTenantUser(userDto, userPrincipal);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TenantUserResponseDto>>> getTenantUsers(@AuthenticationPrincipal UserPrincipal userPrincipal) throws NotFoundException {
        return new ResponseBuilder<List<TenantUserResponseDto>>().body(tenantService.getUsers(userPrincipal)).build();
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteUserMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        tenantService.deleteUserFromTenant(userPrincipal);
    }

    @PostMapping("/users/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void blockUserMapping(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        tenantService.blockUserFromTenant(userPrincipal);
    }

    @PostMapping("/create-agency")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<TenantDto>> createAgency(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid Tenant tenant) {
        return new ResponseBuilder<TenantDto>()
                .body(tenantService.createAgency(tenant, userPrincipal))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/get-assignable-tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<
            Map<TenantType, List<AssignableTenant>
                    >>> getAgencies(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return new ResponseBuilder<Map<TenantType, List<AssignableTenant>>>()
                .body(tenantService.getAssignableTenantsOrAgencies(userPrincipal))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @PostMapping("/{agencyId}/add-user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void addAgencyUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID agencyId, @RequestBody @Valid UserDto userDto) throws IOException {
        tenantService.addAgencyUser(userPrincipal, agencyId, userDto);
    }

    @PostMapping("/{agencyId}/add-existing-user")
    @ResponseStatus(HttpStatus.OK)
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public void addAgencyUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID agencyId, @RequestBody @Valid AddExistingUserDto addExistingUserDto) throws IOException {
        tenantService.addAgencyUser(userPrincipal, agencyId, addExistingUserDto);
    }
}
