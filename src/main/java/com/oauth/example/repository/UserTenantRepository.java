package com.oauth.example.repository;

import com.oauth.example.domain.entity.UserTenant;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserTenantRepository extends BaseRepository<UserTenant, UUID> {
    @Query(value = "select ut from UserTenant ut where ut.tenant.id = :tenantId and ut.user.id = :userId and ut.status = 'ACTIVE'")
    Optional<List<UserTenant>> findIdByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query(value = """
            select ut
                from
            UserTenant ut
            left join fetch ut.tenant
            left join fetch ut.role
            left join fetch ut.user
            where ut.user.id = :userId
            and ut.tenant.id = :tenantId
            """
    )
    Optional<UserTenant> findUserDetailsByUserIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);


    @Query("SELECT ut from UserTenant ut where ut.tenant.id = :tenantId and deletedAt IS null")
    List<UserTenant> findUsersByTenantId(@Param("tenantId") UUID tenantId);

    @Query(value = "select distinct(ut.tenant_id) as \"tenantId\", t.type as type from user_tenant ut join tenant t on ut.tenant_id = t.id where ut.user_id = :userId and ut.status = 'ACTIVE'", nativeQuery = true)
    Set<AssignableTenant> getTenants(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "insert into user_tenant(tenant_id, user_id, role_id, status) values(:tenantId, :userId, :roleId, 'ACTIVE')", nativeQuery = true)
    void addUserTenant(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query(value = """
            select
                ut
            from
                UserTenant ut
            where ut.user.id = :userId
            and ut.role.id = :roleId
            """)
    UserTenant findUserTenantByUserIdAndRoleId(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Query(value = """
            select ut
                from
            UserTenant ut
            left join fetch ut.tenant
            left join fetch ut.role
            left join fetch ut.user
            where ut.user.id = :userId"""
    )
    Optional<UserTenant> findByUserId(@Param("userId") UUID userId);
}
