package com.oauth.example.repository;

import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenantRepository extends BaseRepository<Tenant, UUID> {

    @Modifying
    @Query(value = "update user_tenant set deleted_at = now() where user_id = :userId and tenant_id = :tenantId", nativeQuery = true)
    void deleteByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Modifying
    @Query(value = "update user_tenant set status = 'BLOCKED' where user_id = :userId and tenant_id = :tenantId", nativeQuery = true)
    void blockByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query(value = "select t.id as \"tenantId\", t.type, t.name from user_tenant ut join tenant t on ut.tenant_id = t.id where ut.user_id = :userId", nativeQuery = true)
    List<AssignableTenant> getAssignableTenantsOrAgencies(@Param("userId") UUID userId);
}
