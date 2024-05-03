package com.oauth.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Getter
@Setter
public class Role extends BaseEntity {

    @Column()
    private String name;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private Set<RolePermission> permissions;

    @Column()
    private String description;

    @Column()
    private String status;

}
