package com.oauth.example.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Getter
@Setter
public class Permission extends BaseEntity {

    @Column()
    private String name;

    @OneToMany(mappedBy = "permission")
    private Set<RolePermission> roles;

    @Column()
    private String description;

    @Column()
    private String status;

}
