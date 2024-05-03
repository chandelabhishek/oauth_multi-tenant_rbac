package com.oauth.example.repository;

import com.oauth.example.domain.entity.User;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
