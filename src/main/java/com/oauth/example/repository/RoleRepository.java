package com.oauth.example.repository;

import com.oauth.example.domain.entity.Role;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends BaseRepository<Role, UUID> {
    Role findByName(String name);
}
