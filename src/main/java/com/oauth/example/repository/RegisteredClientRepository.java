package com.oauth.example.repository;

import com.oauth.example.domain.entity.DomainRegisteredClient;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisteredClientRepository extends BaseRepository<DomainRegisteredClient, UUID> {
    @Query(value = "select * from oauth2_registered_client where client_id = :clientId", nativeQuery = true)
    Optional<DomainRegisteredClient> findByClientId(@Param("clientId") String clientId);
}
