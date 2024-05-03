package com.oauth.example.service;

import com.oauth.example.domain.dto.AcceptUserInviteDto;
import com.oauth.example.domain.dto.PasswordChangeRequest;
import com.oauth.example.domain.dto.SignUpRequest;
import com.oauth.example.domain.dto.UserMeDto;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.exception.InvalidTokenException;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.exception.UserPasswordDidNotMatchException;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.repository.UserRepository;
import com.oauth.example.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserTenantRepository userTenantRepository;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;
    private final UserPrincipal userPrincipal;


    public User save(SignUpRequest signUpRequest) {
        signUpRequest.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        return userRepository.save(modelMapper.map(signUpRequest, User.class));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        userPrincipal.setUser(user);
        return userPrincipal;
    }

    public UserPrincipal findUserDetailsById(UUID id) throws UsernameNotFoundException {
        var user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        userPrincipal.setUser(user);
        return userPrincipal;
    }

    public UserPrincipal findUserDetailsByUserIdAndTenantId(UUID userId, UUID tenantId) throws UsernameNotFoundException {
        var userTenant = userTenantRepository.findUserDetailsByUserIdAndTenantId(userId, tenantId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userPrincipal.setUser(userTenant.getUser());
        return userPrincipal;
    }

    @Transactional
    public UserMeDto me(UserPrincipal userPrincipal) {
        var propertyMapper = modelMapper.typeMap(User.class, UserMeDto.class);
        var activeTenant = userPrincipal.getUser().getCurrentTenant();
        if (activeTenant.isPresent()) {
            propertyMapper.addMapping(User::getCurrentTenant, UserMeDto::setTenant);
        }
        var response = modelMapper.map(userPrincipal.getUser(), UserMeDto.class);
        response.setCurrentRoles(userPrincipal.getUser().getCurrentRoleNames());
        return response;
    }

    public void changePassword(UserPrincipal userPrincipal, PasswordChangeRequest passwordChangeRequest) {
        var user = userPrincipal.getUser();
        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())) {
            throw new UserPasswordDidNotMatchException("oldPassword and user's current password does not match");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        user.setLastPasswordChangedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    private NotFoundException userNotFound() {
        return new NotFoundException("User not found");
    }

    public void acceptUserInvite(AcceptUserInviteDto acceptUserInviteDto) throws NotFoundException {
        var decryptedText = cryptoService.decrypt(acceptUserInviteDto.getToken()).split(":");
        if (decryptedText.length < 2) {
            throw new InvalidTokenException("Invalid token supplied");
        }
        var email = decryptedText[0];
        var incomingTenantId = decryptedText[1];

        var user = userRepository.findByEmail(email).orElseThrow(this::userNotFound);
        var userTenant = user.getInvitedUserTenant().orElseThrow(this::userNotFound);
        var activeTenantId = userTenant.getTenant().getId().toString();
        if (!activeTenantId.equals(incomingTenantId)) {
            throw userNotFound();
        }
        userTenant.setStatus(UserStatus.ACTIVE);
        userTenantRepository.save(userTenant);
    }

    public Set<AssignableTenant> getTenants(UUID userId) {
        return userTenantRepository.getTenants(userId);
    }
}
