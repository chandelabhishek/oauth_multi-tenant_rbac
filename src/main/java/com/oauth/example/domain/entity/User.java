package com.oauth.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth.example.domain.enums.UserStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "\"user\"")
@Getter
@Setter
public class User extends BaseEntity {
    @NotBlank(message = "firstName is mandatory")
    private String firstName;
    private String lastName;

    @NotBlank(message = "email is mandatory")
    private String email;
    private String phoneNumber;
    private String countryCode;

    @NotBlank(message = "password is mandatory")
    private String password;
    private OffsetDateTime lastPasswordChangedAt;

    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private Map<String, Object> config;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
    private String secureCode;

    @Transient
    private Tenant currentTenant;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserTenant> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.JOIN)
    private Set<UserTenant> userTenants;

    @Transient
    private List<String> currentRoleNames = new ArrayList<>();

    @JsonIgnore
    public Set<UserTenant> getUserTenantsWithStatus(UserStatus userStatus) {
        var uts = this.getUserTenants();
        return uts.stream().filter(
                userTenant -> userTenant.getStatus()
                        .equals(userStatus) && userTenant.getDeletedAt() == null
        ).collect(Collectors.toUnmodifiableSet());
    }

    @JsonIgnore
    public Optional<UserTenant> getUserTenantWithStatus(UserStatus userStatus) {
        return getUserTenantsWithStatus(userStatus).stream().findFirst();
    }

    @JsonIgnore
    public Optional<Tenant> getCurrentTenant() {
        return Optional.ofNullable(currentTenant);
    }

    @JsonIgnore
    public void setCurrentTenant(Tenant tenant) {
        currentTenant = tenant;
    }

    @JsonIgnore
    public Optional<UserTenant> getInvitedUserTenant() {
        return getUserTenantWithStatus(UserStatus.INVITED);
    }

    @JsonIgnore
    public Set<UserTenant> getActiveUserTenants() {
        return getUserTenantsWithStatus(UserStatus.ACTIVE);
    }

    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        if (getCurrentTenant().isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("TENANT_NOT_AVAILABLE"));
            return authorities;
        }
        return authorities;
    }
}
