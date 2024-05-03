package com.oauth.example.utils;

import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.dto.UserMeDto;
import com.oauth.example.domain.entity.Role;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.entity.UserTenant;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.model.TenantConfig;
import com.oauth.example.domain.model.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestUtils {

    public static UserPrincipal getUserPrincipal(UUID tenantId, User user) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("JOHN WICK");
        user.setCurrentTenant(tenant);
        UserTenant userTenant = new UserTenant();
        userTenant.setUser(user);
        userTenant.setTenant(tenant);
        user.setUserTenants(Set.of(userTenant));
        return new UserPrincipal(user);
    }

    public static User getUser(UUID userId, UUID tenantId) {
        var user = new User();
        user.setId(userId);
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        user.setCurrentTenant(tenant);
        return user;
    }

    public static Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setAddress("42 Main St");
        tenant.setCity("Oxford");
        tenant.setConfig(new TenantConfig());
        tenant.setCountry("GB");
        tenant.setCountryCode("GB");
        tenant.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        tenant.setDeletedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant.setEmail("jane.doe@example.org");
        tenant.setId(UUID.randomUUID());
        tenant.setName("Name");
        tenant.setPhoneNumber("6625550144");
        tenant.setPostalCode("Postal Code");
        tenant.setState("MD");
        tenant.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant.setUpdatedBy("2020-03-01");
        tenant.setUserTenants(new HashSet<>());
        return tenant;
    }

    public static TenantDto getTenantDto(UUID id) {
        TenantDto tenantDto = new TenantDto();
        tenantDto.setAddress("42 Main St");
        tenantDto.setCity("Oxford");
        tenantDto.setClientId("42");
        tenantDto.setConfig(new TenantConfig());
        tenantDto.setCountry("GB");
        tenantDto.setCountryCode("GB");
        tenantDto.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenantDto.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        tenantDto.setEmail("jane.doe@example.org");
        tenantDto.setHasOwnCredentials(true);
        tenantDto.setId(id == null ? UUID.randomUUID() : id);
        tenantDto.setName("Name");
        tenantDto.setPhoneNumber("6625550144");
        tenantDto.setPostalCode("Postal Code");
        tenantDto.setState("MD");
        tenantDto.setStatus("Status");
        tenantDto.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenantDto.setUpdatedBy("2020-03-01");
        return tenantDto;
    }

    public static User getUser(Tenant tenant) {
        User user = new User();
        user.setConfig(new HashMap<>());
        user.setCountryCode("GB");
        user.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        user.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        user.setCurrentTenant(tenant);
        user.setDeletedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        user.setEmail("jane.doe@example.org");
        user.setFirstName("Jane");
        user.setId(UUID.randomUUID());
        user.setLastName("Doe");
        user.setLastPasswordChangedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        user.setPassword("i_am_the_one_who_knocks");
        user.setPhoneNumber("6625550144");
        user.setSecureCode("Secure Code");
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        user.setUpdatedBy("2020-03-01");
        user.setUserTenants(new HashSet<>());
        user.setCurrentTenant(tenant);
        return user;
    }

    public static User getUser() {
        return getUser(getTenant());
    }

    public static UserMeDto getUserMeDto(Tenant tenant) {
        UserMeDto userMeDto = new UserMeDto();
        userMeDto.setConfig(new HashMap<>());
        userMeDto.setCountryCode("GB");
        userMeDto.setEmail("jane.doe@example.org");
        userMeDto.setFirstName("Jane");
        userMeDto.setLastName("Doe");
        userMeDto
                .setLastPasswordChangedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        userMeDto.setPhoneNumber("6625550144");
        userMeDto.setStatus(UserStatus.ACTIVE);
        userMeDto.setTenant(tenant);
        return userMeDto;
    }

    public static UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setCountryCode("GB");
        userDto.setEmail("someemail@gmail.com");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("pass");
        userDto.setPhoneNumber("+917415222399");
        userDto.setSecureCode("Secure Code");
        return userDto;
    }

    public static Role getRole() {
        Role role = new Role();
        role.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        role.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        role.setDeletedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        role.setDescription("The characteristics of someone or something");
        role.setId(UUID.randomUUID());
        role.setName("Name");
        role.setPermissions(new HashSet<>());
        role.setStatus("Status");
        role.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        role.setUpdatedBy("2020-03-01");
        return role;
    }

    public static UserTenant getUserTenant() {
        Tenant tenant = getTenant();
        return getUserTenant(getUser(tenant), tenant, getRole());
    }

    public static UserTenant getUserTenant(User user, Tenant tenant, Role role) {
        UserTenant userTenant = new UserTenant();
        userTenant.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        userTenant.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        userTenant.setDeletedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        userTenant.setId(UUID.randomUUID());
        userTenant.setRole(role);
        userTenant.setStatus(UserStatus.ACTIVE);
        userTenant.setTenant(tenant);
        userTenant.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        userTenant.setUpdatedBy("2020-03-01");
        userTenant.setUser(user);
        return userTenant;
    }
}
