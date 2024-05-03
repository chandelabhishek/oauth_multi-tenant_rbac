package com.oauth.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oauth.example.domain.dto.AuthResponse;
import com.oauth.example.domain.enums.TenantType;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.model.TenantConfig;
import com.oauth.example.domain.model.TenantWithToken;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.modelmapper.ModelMapper;

import java.util.Set;


@Entity
@Getter
@Setter
public class Tenant extends BaseEntity {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "email is mandatory")
    private String email;

    @NotBlank(message = "phoneNumber is mandatory")
    private String phoneNumber;

    @NotBlank(message = "countryCode is mandatory")
    private String countryCode;

    @NotBlank(message = "address is mandatory")
    private String address;

    @NotBlank(message = "state is mandatory")
    private String state;

    @NotNull
    private String country;

    @NotNull
    private String city;

    @NotNull
    private String postalCode;

    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private TenantConfig config;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tenant parent;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<Tenant> agencies;

    @Enumerated(EnumType.STRING)
    private TenantType type;

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private Set<UserTenant> userTenants;

    public Tenant addUser(User user) {
        UserTenant userTenant = new UserTenant();
        userTenant.setUser(user);
        userTenant.setTenant(this);
        userTenant.setStatus(UserStatus.ACTIVE);
        this.userTenants.add(userTenant);
        return this;
    }

    public TenantWithToken withToken(AuthResponse token) {
        ModelMapper modelMapper = new ModelMapper();
        var tenantWithToken = modelMapper.map(this, TenantWithToken.class);
        tenantWithToken.setAccessToken(token.getAccessToken());
        tenantWithToken.setRefreshToken(token.getRefreshToken());
        return tenantWithToken;
    }
}
