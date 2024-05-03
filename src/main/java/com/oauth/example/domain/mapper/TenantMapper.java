package com.oauth.example.domain.mapper;

import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.entity.Tenant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTenantFromDto(TenantDto dto, @MappingTarget Tenant entity);
}
