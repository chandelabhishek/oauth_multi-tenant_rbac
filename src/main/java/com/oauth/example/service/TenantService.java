package com.oauth.example.service;

import com.oauth.example.domain.dto.AddExistingUserDto;
import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.dto.TenantUserResponseDto;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.entity.UserTenant;
import com.oauth.example.domain.enums.Authority;
import com.oauth.example.domain.enums.TenantType;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.mapper.TenantMapper;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.repository.RoleRepository;
import com.oauth.example.repository.TenantRepository;
import com.oauth.example.repository.UserRepository;
import com.oauth.example.repository.UserTenantRepository;
import com.oauth.example.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TenantService {
    private static final Logger logger = LogManager.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserTenantRepository userTenantRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final CryptoService cryptoService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService;
    private final TenantMapper tenantMapper;
    @Value("${app.frontendUrl}")
    private String frontendUrl;

    public static AccessDeniedException accessDeniedException() {
        return new AccessDeniedException("You do not have proper permissions");
    }

    public Iterable<Tenant> findAll() {
        return tenantRepository.findAll(Sort.by("id"));
    }

    public Tenant getTenant(final UserPrincipal userPrincipal) throws NotFoundException {
        return userPrincipal.getUser().getCurrentTenant().orElseThrow(this::notFoundException);
    }

    @Transactional
    public TenantDto createTenant(Tenant tenant) {
        tenant.setType(tenant.getType() == null ? TenantType.TENANT : tenant.getType());
        var savedTenant = tenantRepository.save(tenant);
        return modelMapper.map(savedTenant, TenantDto.class);
    }

    @Transactional
    public TenantDto update(final TenantDto tenantDto) throws NotFoundException {
        var tenant = tenantRepository.findById(tenantDto.getId()).orElseThrow(() -> new NotFoundException("Tenant Not Found"));
        tenantMapper.updateTenantFromDto(tenantDto, tenant);
        var savedTenant = tenantRepository.save(tenant);
        return modelMapper.map(savedTenant, TenantDto.class);
    }

    private String createUserInviteLink(User user, UUID tenantId, String tenantName) {
        var textToEncrypt = String.format("%s:%s", user.getEmail(), tenantId);
        return String.format("%s?token=%s&tenantName=%s", frontendUrl, cryptoService.encrypt(textToEncrypt), tenantName);
    }

    private NotFoundException notFoundException() {
        return new NotFoundException("Tenant was not found");
    }

    @Transactional
    public void addNewUserToTenant(Tenant tenant, User user, Authority authority) throws IOException {
        // adding new user to the tenant
        var role = roleRepository.findByName(authority.name().substring(6)); // removing SCOPE_ from authority
        var newUserTenant = new UserTenant();
        newUserTenant.setUser(user);
        newUserTenant.setTenant(tenant);
        newUserTenant.setStatus(UserStatus.INVITED);
        newUserTenant.setRole(role);
        userTenantRepository.save(newUserTenant);
        emailService.sendUserInviteEmail(user, createUserInviteLink(user, tenant.getId(), tenant.getName()), tenant);
    }

    @Transactional
    public void addTenantUserWithRole(Tenant tenant, UserDto userDto, Authority authority) throws IOException {
        // checking if this logged-in user and can add another user
        var userToSave = modelMapper.map(userDto, User.class);
        userToSave.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // adding new user to the tenant
        var savedUser = userRepository.save(userToSave);
        addNewUserToTenant(tenant, savedUser, authority);
    }

    private void checkIfUserBelongsToTenant(User user, Tenant tenant) {
        userTenantRepository.findIdByUserIdAndTenantId(user.getId(), tenant.getId()).orElseThrow(this::notFoundException);
    }

    public void addTenantUser(UserDto userDto, UserPrincipal userPrincipal) throws NotFoundException, IOException {
        var user = userPrincipal.getUser();
        var activeTenant = user.getCurrentTenant().orElseThrow(this::notFoundException);
        checkIfUserBelongsToTenant(user, activeTenant);
        addTenantUserWithRole(activeTenant, userDto, Authority.SCOPE_TENANT_ADMIN);
    }

    public void addTenantUser(UserDto userDto, UUID tenantId) throws NotFoundException, IOException {
        var tenant = tenantRepository.findById(tenantId).orElseThrow(this::notFoundException);
        addTenantUserWithRole(tenant, userDto, Authority.SCOPE_TENANT_ADMIN);
    }

    @Transactional
    public List<TenantUserResponseDto> getUsers(UserPrincipal userPrincipal) throws NotFoundException {
        List<TenantUserResponseDto> response = new ArrayList<>();
        var tenant = userPrincipal.getUser().getCurrentTenant().orElseThrow(this::notFoundException);
        var userTenants = userTenantRepository.findUsersByTenantId(tenant.getId());
        userTenants.forEach(userTenant -> {
            var mappedUser = modelMapper.map(userTenant.getUser(), TenantUserResponseDto.class);
            mappedUser.setStatus(userTenant.getStatus());
            response.add(mappedUser);
        });
        return response;
    }

    public void deleteUserFromTenant(UserPrincipal userPrincipal) {
        tenantRepository.deleteByUserIdAndTenantId(userPrincipal.getUser().getId(), userPrincipal.getTenantId().orElse(null));
    }

    public void blockUserFromTenant(UserPrincipal userPrincipal) {
        tenantRepository.blockByUserIdAndTenantId(userPrincipal.getUser().getId(), userPrincipal.getTenantId().orElse(null));
    }

    public TenantDto createAgency(Tenant tenant, UserPrincipal userPrincipal) {
        var parentTenantId = userPrincipal.getTenantId().orElseThrow(TenantService::accessDeniedException);
        tenant.setParent(tenantRepository.getReferenceById(parentTenantId));
        tenant.setType(TenantType.AGENCY);
        return createTenant(tenant);
    }

    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id);
    }

    public Map<TenantType, List<AssignableTenant>> getAssignableTenantsOrAgencies(UserPrincipal userPrincipal) {
        return tenantRepository.getAssignableTenantsOrAgencies(userPrincipal.getId())
                .stream().collect(Collectors.groupingBy(AssignableTenant::getType));
    }

    @Transactional
    public void addAgencyUser(UserPrincipal userPrincipal, UUID agencyId, UserDto userDto) throws IOException {
        var currentTenant = userPrincipal.getUser().getCurrentTenant().orElseThrow(TenantService::accessDeniedException);
        var agency = tenantRepository.findById(agencyId).orElseThrow(() -> new NotFoundException("Agency does not exist"));
        //check if supplied agency is child of the logged-in tenant
        if (!agency.getParent().getId().equals(currentTenant.getId())) {
            throw accessDeniedException();
        }
        addTenantUserWithRole(agency, userDto, Authority.SCOPE_AGENCY_ADMIN);
    }

    @Transactional
    public void addAgencyUser(UserPrincipal userPrincipal, UUID agencyId, AddExistingUserDto addExistingUserDto) throws IOException {
        var currentTenant = userPrincipal.getUser().getCurrentTenant().orElseThrow(TenantService::accessDeniedException);
        var agency = tenantRepository.findById(agencyId).orElseThrow(() -> new NotFoundException("Agency does not exist"));
        //check if supplied agency is child of the logged-in tenant
        if (!agency.getParent().getId().equals(currentTenant.getId())) {
            throw accessDeniedException();
        }
        var userToAdd = userRepository.findById(addExistingUserDto.getUserId()).orElseThrow(() -> new NotFoundException("userId:" + addExistingUserDto.getUserId() + " not found"));
        addNewUserToTenant(agency, userToAdd, Authority.SCOPE_AGENCY_ADMIN);
    }

    public void addTenantUser(UUID tenantId, UUID userId) {
        var role = roleRepository.findByName(Authority.SCOPE_TENANT_ADMIN.name());
        userTenantRepository.addUserTenant(tenantId, userId, role.getId());
    }
}




