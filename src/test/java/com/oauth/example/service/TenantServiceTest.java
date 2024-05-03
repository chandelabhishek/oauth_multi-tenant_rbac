package com.oauth.example.service;
import com.oauth.example.domain.dto.AuthResponse;
import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.dto.TenantUserResponseDto;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.entity.Role;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.entity.UserTenant;
import com.oauth.example.domain.enums.TenantType;
import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.mapper.TenantMapper;
import com.oauth.example.domain.model.AssignableTenant;
import com.oauth.example.domain.model.TenantConfig;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.repository.RoleRepository;
import com.oauth.example.repository.TenantRepository;
import com.oauth.example.repository.UserRepository;
import com.oauth.example.repository.UserTenantRepository;
import com.oauth.example.service.email.EmailService;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {TenantService.class, BCryptPasswordEncoder.class, ModelMapper.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
class TenantServiceTest {

    @MockBean
    private AuthService authService;

    @MockBean
    private CryptoService cryptoService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private TenantRepository tenantRepository;

    @Autowired
    private TenantService tenantService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserTenantRepository userTenantRepository;

    @MockBean
    private TenantMapper tenantMapper;

    /**
     * Method under test: {@link TenantService#findAll()}
     */
    @Test
    void test_findAll() {
        ArrayList<Tenant> tenantList = new ArrayList<>();
        when(tenantRepository.findAll(Mockito.<Sort>any())).thenReturn(tenantList);
        Iterable<Tenant> actualFindAllResult = tenantService.findAll();
        assertSame(tenantList, actualFindAllResult);
        assertTrue(((Collection<Tenant>) actualFindAllResult).isEmpty());
        verify(tenantRepository).findAll(Mockito.<Sort>any());
    }

    /**
     * Method under test: {@link TenantService#findAll()}
     */
    @Test
    void test_findAll_throws_Exception() {
        when(tenantRepository.findAll(Mockito.<Sort>any())).thenThrow(new IllegalArgumentException("id"));
        assertThrows(IllegalArgumentException.class, () -> tenantService.findAll());
        verify(tenantRepository).findAll(Mockito.<Sort>any());
    }

    /**
     * Method under test: {@link TenantService#getTenant(UserPrincipal)}
     */
    @Test
    void test_getTenant_throws_NotFoundException() throws NotFoundException {
        assertThrows(NotFoundException.class, () -> tenantService.getTenant(new UserPrincipal(new User())));
    }

    /**
     * Method under test: {@link TenantService#getTenant(UserPrincipal)}
     */
    @Test
    void test_getTenant_success() throws NotFoundException {
        Tenant tenant = TestUtils.getTenant();
        Optional<Tenant> ofResult = Optional.of(tenant);
        User user = mock(User.class);
        when(user.getCurrentTenant()).thenReturn(ofResult);
        assertSame(tenant, tenantService.getTenant(new UserPrincipal(user)));
    }

    /**
     * Method under test: {@link TenantService#createTenant(Tenant)}
     */
    @Test
    void test_create_success() {
        when(authService.generateToken(Mockito.any())).thenReturn(new AuthResponse());

        Tenant tenant = TestUtils.getTenant();
        when(tenantRepository.save(Mockito.any())).thenReturn(tenant);
        when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(TestUtils.getTenantDto(tenant.getId()));
        Role role = TestUtils.getRole();

        Tenant currentTenant = TestUtils.getTenant();
        User user = TestUtils.getUser(currentTenant);


        TenantDto actualCreateResult = tenantService.createTenant(tenant);
        assertEquals("Postal Code", actualCreateResult.getPostalCode());
        assertEquals("6625550144", actualCreateResult.getPhoneNumber());
        assertEquals("Name", actualCreateResult.getName());
        assertSame(tenant.getId(), actualCreateResult.getId());
        assertEquals("GB", actualCreateResult.getCountry());
        assertEquals("GB", actualCreateResult.getCountryCode());
        assertEquals("42", actualCreateResult.getClientId());
        verify(tenantRepository).save(Mockito.any());
    }

    /**
     * Method under test: {@link TenantService#update(TenantDto)}
     */
    @Test
    void test_update_success() throws NotFoundException {
        when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(null);

        Tenant tenant = TestUtils.getTenant();
        Optional<Tenant> ofResult = Optional.of(tenant);

        when(tenantRepository.save(Mockito.any())).thenReturn(tenant);
        when(tenantRepository.findById(Mockito.any())).thenReturn(ofResult);

        TenantDto tenant3 = new TenantDto();
        tenant3.setAddress("42 Main St");
        tenant3.setClientId("42");
        tenant3.setConfig(new TenantConfig());
        tenant3.setCountry("GB");
        tenant3.setCountryCode("GB");
        tenant3.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant3.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        tenant3.setEmail("jane.doe@example.org");
        tenant3.setHasOwnCredentials(true);
        tenant3.setId(UUID.randomUUID());
        tenant3.setName("Name");
        tenant3.setPhoneNumber("6625550144");
        tenant3.setState("MD");
        tenant3.setStatus("Status");
        tenant3.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant3.setUpdatedBy("2020-03-01");
        assertNull(tenantService.update(tenant3));
        verify(modelMapper, atLeast(1)).map(Mockito.any(), Mockito.any());
        verify(tenantRepository).save(Mockito.any());
        verify(tenantRepository).findById(Mockito.any());
    }

    /**
     * Method under test: {@link TenantService#update(TenantDto)}
     */
    @Test
    void test_update_throws_NotFoundException_when_could_not_find_tenant() throws NotFoundException {
        when(tenantRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        TenantDto tenant3 = new TenantDto();
        tenant3.setAddress("42 Main St");
        tenant3.setClientId("42");
        tenant3.setConfig(new TenantConfig());
        tenant3.setCountry("GB");
        tenant3.setCountryCode("GB");
        tenant3.setCreatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant3.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        tenant3.setEmail("jane.doe@example.org");
        tenant3.setHasOwnCredentials(true);
        tenant3.setId(UUID.randomUUID());
        tenant3.setName("Name");
        tenant3.setPhoneNumber("6625550144");
        tenant3.setState("MD");
        tenant3.setStatus("Status");
        tenant3.setUpdatedAt(OffsetDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC));
        tenant3.setUpdatedBy("2020-03-01");
        assertThrows(NotFoundException.class, () -> tenantService.update(tenant3));

    }

    /**
     * Method under test: {@link TenantService#addTenantUser(UserDto, UserPrincipal)}
     */
    @Test
    void test_addTenantUser_throws_Exception_when_user_is_empty() throws Exception {
        var user = TestUtils.getUser(null);
        UserDto userDto = new UserDto();
        userDto.setCountryCode("GB");
        userDto.setEmail("jane.doe@example.org");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("password");
        userDto.setPhoneNumber("6625550144");
        userDto.setSecureCode("Secure Code");
        assertThrows(NotFoundException.class, () -> tenantService.addTenantUser(userDto, new UserPrincipal(user)));
    }

    /**
     * Method under test: {@link TenantService#addTenantUser(UserDto, UserPrincipal)}
     */
    @Test
    void test_addTenantUser_throws_if_it_is_not_a_valid_user() throws Exception {
        UserDto userDto = mock(UserDto.class);
        User user = TestUtils.getUser();
        userDto.setCountryCode("GB");
        userDto.setEmail("jane.doe@example.org");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("password");
        userDto.setPhoneNumber("6625550144");
        userDto.setSecureCode("Secure Code");
        when(userTenantRepository.findOne(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> tenantService.addTenantUser(userDto, new UserPrincipal(user)));
    }

    /**
     * Method under test: {@link TenantService#addTenantUser(UserDto, UserPrincipal)}
     */
    @Test
    void test_addTenantUser_success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setCountryCode("GB");
        userDto.setEmail("jane.doe@example.org");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("password");
        userDto.setPhoneNumber("6625550144");
        userDto.setSecureCode("Secure Code");

        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        UserTenant userTenant = TestUtils.getUserTenant(user, tenant, new Role());
        when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(user);
        when(userTenantRepository.findIdByUserIdAndTenantId(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(List.of(userTenant)));
        when(userRepository.save(Mockito.any()))
                .thenReturn(user);
        when(cryptoService.encrypt(Mockito.any())).thenReturn("encryptedToken");
        Optional<Tenant> ofResult = Optional.of(tenant);
        tenantService.addTenantUser(userDto, new UserPrincipal(user));
        verify(modelMapper).map(Mockito.any(), Mockito.any());
        verify(userTenantRepository).findIdByUserIdAndTenantId(Mockito.any(), Mockito.any());
        String frontendUrl = "http://localhost:8080/set-new-password";
        verify(emailService).sendUserInviteEmail(eq(user), eq(String.format("%s?token=encryptedToken&tenantName=Name", frontendUrl)), Mockito.any());
    }

    @Test
    void test_addTenantUser_UserDto_TenantId_success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setCountryCode("GB");
        userDto.setEmail("jane.doe@example.org");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("password");
        userDto.setPhoneNumber("6625550144");
        userDto.setSecureCode("Secure Code");

        Tenant tenant = TestUtils.getTenant();
        User user = TestUtils.getUser(tenant);
        UserTenant userTenant = TestUtils.getUserTenant(user, tenant, new Role());
        when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(user);
        when(userRepository.save(Mockito.any()))
                .thenReturn(user);
        when(cryptoService.encrypt(Mockito.any())).thenReturn("encryptedToken");
        Optional<Tenant> ofResult = Optional.of(tenant);
        when(tenantRepository.findById(Mockito.any(UUID.class))).thenReturn(ofResult);

        tenantService.addTenantUser(userDto, tenant.getId());

        verify(modelMapper).map(Mockito.any(), Mockito.any());
        String frontendUrl = "http://localhost:8080/set-new-password";
        verify(emailService).sendUserInviteEmail(eq(user), eq(String.format("%s?token=encryptedToken&tenantName=Name", frontendUrl)), Mockito.any());
    }

    /**
     * Method under test: {@link TenantService#getUsers(UserPrincipal)}
     */
    @Test
    void test_getUsers_throws_NotFoundException_when_user_not_found() throws NotFoundException {
        assertThrows(NotFoundException.class, () -> tenantService.getUsers(new UserPrincipal(new User())));
    }

    /**
     * Method under test: {@link TenantService#getUsers(UserPrincipal)}
     */
    @Test
    void test_getUsers_success() throws NotFoundException {
        var userList = new ArrayList<UserTenant>();
        Tenant tenant = TestUtils.getTenant();
        var user1 = TestUtils.getUser(tenant);
        userList.add(TestUtils.getUserTenant(user1, tenant, new Role()));
        when(userTenantRepository.findUsersByTenantId(Mockito.any())).thenReturn(userList);
        var userTenantResponse = new TenantUserResponseDto();
        userTenantResponse.setFirstName(user1.getFirstName());
        userTenantResponse.setLastName(user1.getLastName());
        userTenantResponse.setId(user1.getId());
        userTenantResponse.setEmail(user1.getEmail());

        when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(userTenantResponse);
        var users = tenantService.getUsers(new UserPrincipal(user1));
        assertSame(users.get(0).getFirstName(), user1.getFirstName());
        assertSame(users.get(0).getLastName(), user1.getLastName());
        assertSame(users.get(0).getEmail(), user1.getEmail());
        assertSame(users.get(0).getId(), user1.getId());
        verify(userTenantRepository).findUsersByTenantId(Mockito.any());
    }

    /**
     * Method under test: {@link TenantService#deleteUserFromTenant(UserPrincipal)}
     */
    @Test
    void test_deleteUserFromTenant_success() {
        doNothing().when(tenantRepository).deleteByUserIdAndTenantId(Mockito.any(), Mockito.any());
        var tenant = TestUtils.getTenant();
        var user = TestUtils.getUser(tenant);
        tenantService.deleteUserFromTenant(new UserPrincipal(user));
        verify(tenantRepository).deleteByUserIdAndTenantId(user.getId(), tenant.getId());
    }

    /**
     * Method under test: {@link TenantService#blockUserFromTenant(UserPrincipal)}
     */

    @Test
    void test_blockUserFromTenant_success() {
        doNothing().when(tenantRepository).blockByUserIdAndTenantId(Mockito.any(), Mockito.any());
        Tenant tenant = TestUtils.getTenant();
        Optional<Tenant> ofResult = Optional.of(tenant);
        User user = mock(User.class);
        when(user.getCurrentTenant()).thenReturn(ofResult);
        when(user.getId()).thenReturn(UUID.randomUUID());
        tenantService.blockUserFromTenant(new UserPrincipal(user));
        verify(tenantRepository).blockByUserIdAndTenantId(Mockito.any(), Mockito.any());
        verify(user).getCurrentTenant();
        verify(user).getId();
        assertTrue(((Collection<Tenant>) tenantService.findAll()).isEmpty());
    }

    @Test
    void test_createAgency_success() {
        Tenant tenant = TestUtils.getTenant();
        Tenant parent = TestUtils.getTenant();
        when(tenantRepository.getReferenceById(tenant.getId())).thenReturn(parent);
        User user = TestUtils.getUser(tenant);
        UserPrincipal userPrincipal = new UserPrincipal(user);

        tenantService.createAgency(tenant, userPrincipal);

        verify(tenantRepository).getReferenceById(tenant.getId());
        assertSame(tenant.getParent(), parent);
        assertSame(TenantType.AGENCY, tenant.getType());
    }

    @Test
    void test_getAssignableTenantsOrAgencies_success() {
        Tenant tenant = TestUtils.getTenant();
        tenant.setType(TenantType.TENANT);
        var assignableTenant = new AssignableTenant() {
            @Override
            public UUID getTenantId() {
                return tenant.getId();
            }

            @Override
            public String getName() {
                return tenant.getName();
            }

            @Override
            public TenantType getType() {
                return tenant.getType();
            }
        };
        when(tenantRepository.getAssignableTenantsOrAgencies(Mockito.any(UUID.class))).thenReturn(List.of(assignableTenant));
        User user = TestUtils.getUser(tenant);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        var res = tenantService.getAssignableTenantsOrAgencies(userPrincipal);

        verify(tenantRepository).getAssignableTenantsOrAgencies(user.getId());
        var expected = new HashMap<TenantType, List<AssignableTenant>>();
        expected.put(TenantType.TENANT, List.of(assignableTenant));
        assertEquals(expected, res);

    }
}

