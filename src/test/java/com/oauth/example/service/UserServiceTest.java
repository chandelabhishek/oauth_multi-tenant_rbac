package com.oauth.example.service;

import com.oauth.example.domain.dto.AcceptUserInviteDto;
import com.oauth.example.domain.dto.PasswordChangeRequest;
import com.oauth.example.domain.dto.UserMeDto;
import com.oauth.example.domain.entity.Role;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.entity.UserTenant;
import com.oauth.example.domain.enums.UserStatus;
import com.oauth.example.domain.exception.InvalidTokenException;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.exception.UserPasswordDidNotMatchException;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.repository.UserRepository;
import com.oauth.example.repository.UserTenantRepository;
import com.oauth.example.utils.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {UserService.class, ModelMapper.class, UserPrincipal.class})
@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @MockBean
    private CryptoService cryptoService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private UserTenantRepository userTenantRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Method under test: {@link UserService#save(User)}
     */
    @Test
    void test_save() {
        var user = TestUtils.getUser();
        when(userRepository.save(user)).thenReturn(user);
        assertSame(user, userService.save(user));
        verify(userRepository).save(user);
    }

    /**
     * Method under test: {@link UserService#findByEmail(String)}
     */
    @Test
    void test_findByEmail_returns_User() {
        Tenant currentTenant = TestUtils.getTenant();
        User user = TestUtils.getUser(currentTenant);
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.any())).thenReturn(ofResult);
        Optional<User> actualFindByEmailResult = userService.findByEmail("jane.doe@example.org");
        assertSame(ofResult, actualFindByEmailResult);
        assertTrue(actualFindByEmailResult.isPresent());
        verify(userRepository).findByEmail(Mockito.any());
    }

    /**
     * Method under test: {@link UserService#loadUserByUsername(String)}
     */
    @Test
    void test_loadUserByUsername_returns_UserDetails() throws UsernameNotFoundException {
        Tenant currentTenant = TestUtils.getTenant();
        User user = TestUtils.getUser(currentTenant);
        Optional<User> ofResult = Optional.of(user);
        when(userRepository.findByEmail(Mockito.any())).thenReturn(ofResult);
        Assertions.assertEquals("i_am_the_one_who_knocks", userService.loadUserByUsername("janedoe").getPassword());
        verify(userRepository).findByEmail(Mockito.any());
    }

    /**
     * Method under test: {@link UserService#loadUserByUsername(String)}
     */
    @Test
    void test_loadUserByUsername_throws_UsernameNotFoundException() throws UsernameNotFoundException {
        Optional<User> emptyResult = Optional.empty();
        when(userRepository.findByEmail(Mockito.any())).thenReturn(emptyResult);
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("janedoe"));
        verify(userRepository).findByEmail(Mockito.any());
    }

    /**
     * Method under test: {@link UserService#me(UserPrincipal)}
     */
    @Test
    void test_me() {
        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        UserMeDto userMeDto = TestUtils.getUserMeDto(tenant);
        userMeDto.setCurrentRoles(user.getCurrentRoleNames());
        assertThat(userMeDto, Matchers.samePropertyValuesAs(userService.me(new UserPrincipal(user))));
    }

    /**
     * Method under test: {@link UserService#changePassword(UserPrincipal, PasswordChangeRequest)}
     */
    @Test
    void test_changePassword_throws_UserPasswordDidNotMatchException() {
        UserPrincipal userPrincipal = new UserPrincipal(new User());
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setNewPassword("i_am_the_one_who_knocks");
        passwordChangeRequest.setOldPassword("i_am_the_one_who_knocks_different");
        assertThrows(UserPasswordDidNotMatchException.class,
                () -> userService.changePassword(userPrincipal, passwordChangeRequest));
    }

    /**
     * Method under test: {@link UserService#changePassword(UserPrincipal, PasswordChangeRequest)}
     */
    @Test
    void test_changePassword_ChangesPasswordSuccessfully() {
        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        UserPrincipal userPrincipal = new UserPrincipal(user);

        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        doReturn(true).when(passwordEncoder).matches(Mockito.any(), Mockito.any());
        passwordChangeRequest.setNewPassword("i_am_the_one_who_knocks_new");
        passwordChangeRequest.setOldPassword("i_am_the_one_who_knocks");
        userService.changePassword(userPrincipal, passwordChangeRequest);
        user.setPassword("i_am_the_one_who_knocks_new");
        user.setLastPasswordChangedAt(OffsetDateTime.now());
        verify(userRepository).save(user);
    }

    /**
     * Method under test: {@link UserService#acceptUserInvite(AcceptUserInviteDto)}
     */
    @Test
    void test_acceptUserInvite_success() throws NotFoundException {
        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
//        UserPrincipal userPrincipal = new UserPrincipal(user);
        UserTenant userTenant = new UserTenant();
        userTenant.setTenant(tenant);
        userTenant.setUser(user);
        userTenant.setStatus(UserStatus.INVITED);
        user.setUserTenants(Set.of(userTenant));
        var decryptedToken = String.format("accept@example.com:%s", tenant.getId().toString());
        var someDummyToken = "someDummyToken";
        when(cryptoService.decrypt(Mockito.any())).thenReturn(decryptedToken);
        when(userRepository.findByEmail(Mockito.any())).thenReturn(Optional.of(user));
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken(someDummyToken);
        userService.acceptUserInvite(acceptUserInviteDto);
        userTenant.setStatus(UserStatus.ACTIVE);
        verify(userTenantRepository).save(userTenant);
    }

    /**
     * Method under test: {@link UserService#acceptUserInvite(AcceptUserInviteDto)}
     */
    @Test
    void test_acceptUserInvite_throws_NotFoundException_when_user_is_empty() throws NotFoundException {
        Tenant tenant = TestUtils.getTenant();
        var decryptedToken = String.format("accept@example.com:%s", tenant.getId().toString());
        when(cryptoService.decrypt(Mockito.any())).thenReturn(decryptedToken);
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        assertThrows(NotFoundException.class, () -> userService.acceptUserInvite(acceptUserInviteDto));
    }

    @Test
    void test_acceptUserInvite_throws_NotFoundException_when_tenant_is_empty() throws NotFoundException {
        User user = TestUtils.getUser(null);
        var decryptedToken = String.format("accept@example.com:%s", UUID.randomUUID());
        when(cryptoService.decrypt(Mockito.any())).thenReturn(decryptedToken);
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        assertThrows(NotFoundException.class, () -> userService.acceptUserInvite(acceptUserInviteDto));
    }

    @Test
    void test_acceptUserInvite_throws_NotFoundException_when_tenant_does_not_match() throws NotFoundException {
        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        var decryptedToken = String.format("accept@example.com:%s", UUID.randomUUID());
        when(cryptoService.decrypt(Mockito.any())).thenReturn(decryptedToken);
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        assertThrows(NotFoundException.class, () -> userService.acceptUserInvite(acceptUserInviteDto));
    }

    @Test
    void test_acceptUserInvite_throws_InvalidTokenException() throws InvalidTokenException {
        when(cryptoService.decrypt(Mockito.any())).thenReturn("some_dummy_token");
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        assertThrows(InvalidTokenException.class, () -> userService.acceptUserInvite(acceptUserInviteDto));
    }

    @Test
    void test_acceptUserInvite_throws_NotFoundException_when_active_tenantId_do_not_match() throws InvalidTokenException {
        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        var userTenant = TestUtils.getUserTenant(user, tenant, new Role());
        userTenant.setStatus(UserStatus.INVITED);
        userTenant.setDeletedAt(null);
        user.setUserTenants(Set.of(userTenant));
        var decryptedToken = String.format("accept@example.com:%s", UUID.randomUUID());
        when(cryptoService.decrypt(Mockito.any())).thenReturn(decryptedToken);
        when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        assertThrows(NotFoundException.class, () -> userService.acceptUserInvite(acceptUserInviteDto));
    }

    @Test
    void test_findUserDetailsById_throws_UsernameNotFoundException_when_user_could_not_be_found() {
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        var id = UUID.randomUUID();
        assertThrows(UsernameNotFoundException.class, () -> userService.findUserDetailsById(id));
    }

    @Test
    void test_findUserDetailsById_success() {
        var user = TestUtils.getUser();
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        assertSame(user, userService.findUserDetailsById(UUID.randomUUID()).getUser());
    }
}

