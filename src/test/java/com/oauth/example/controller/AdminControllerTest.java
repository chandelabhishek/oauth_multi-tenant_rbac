package com.oauth.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.example.domain.dto.LoginRequest;
import com.oauth.example.domain.dto.SignUpRequest;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.TenantService;
import com.oauth.example.service.UserService;
import com.oauth.example.utils.TestUtils;
import org.apache.catalina.User;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.catalina.users.MemoryUserDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AdminController.class})
@SpringBootTest
@ExtendWith(SpringExtension.class)
class AdminControllerTest {
    @Autowired
    private AdminController adminController;

    @MockBean
    private AuthService authService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private UserService userService;

    /**
     * Method under test: {@link AdminController#addTenantUser(UserDto, UUID)}
     */
    @Test
    void test_AddTenantUser_success() throws Exception {
        // Arrange
        doNothing().when(tenantService).addTenantUser(Mockito.<UserDto>any(), Mockito.<UUID>any());
        UserDto userDto = TestUtils.getUserDto();
        String content = (new ObjectMapper()).writeValueAsString(userDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/v1/admin/tenants/{tenantId}/add-user", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(adminController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Method under test: {@link AdminController#addTenantUser(UserDto, UUID)}
     */
    @Test
    void test_AddTenantUser_400() throws Exception {
        // Arrange
        doNothing().when(tenantService).addTenantUser(Mockito.<UserDto>any(), Mockito.<UUID>any());

        UserDto userDto = TestUtils.getUserDto();
        userDto.setPhoneNumber("sdsdsd");
        String content = (new ObjectMapper()).writeValueAsString(userDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/v1/admin/tenants/{tenantId}/add-user", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(adminController)
                .build()
                .perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    /**
     * Method under test: {@link AdminController#createTenant(Tenant)}
     */
    @Test
    void testCreateTenant() throws Exception {
        Tenant parent = TestUtils.getTenant();

        Tenant tenant = TestUtils.getTenant();
        tenant.setParent(parent);

        // setting it to null for jackson binding
        tenant.setCreatedAt(null);
        tenant.setUpdatedAt(null);
        tenant.setDeletedAt(null);

        String content = (new ObjectMapper()).writeValueAsString(tenant);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/admin/create-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        var actualPerformResult = MockMvcBuilders.standaloneSetup(adminController).build().perform(requestBuilder);

        actualPerformResult.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    /**
     * Method under test: {@link AdminController#createUser(SignUpRequest)}
     */
    @Test
    void test_createUser_Success() throws Exception {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setCountryCode("GB");
        signUpRequest.setEmail("jane.doe@example.org");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Doe");
        signUpRequest.setPassword("pass");
        signUpRequest.setPhoneNumber("+917415212396");
        String content = (new ObjectMapper()).writeValueAsString(signUpRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/admin/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(adminController)
                .build()
                .perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    /**
     * Method under test: {@link AdminController#login(LoginRequest)}
     */
    @Test
    void test_login_400() throws Exception {
        // Arrange
        MockHttpServletRequestBuilder contentTypeResult = MockMvcRequestBuilders.post("/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content(objectMapper.writeValueAsString(new LoginRequest()));

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(adminController)
                .build()
                .perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    /**
     * Method under test: {@link AdminController#login(LoginRequest)}
     */
    @Test
    void test_login_success() throws Exception {
        // Arrange
        User user = mock(User.class);
        when(user.getName()).thenReturn("Name");
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/admin/login");
        postResult.principal(new UserDatabaseRealm.UserDatabasePrincipal(user, new MemoryUserDatabase()));
        MockHttpServletRequestBuilder contentTypeResult = postResult.contentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content(objectMapper.writeValueAsString(new LoginRequest("someemail@gmail.com", "some_pass")));

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(adminController)
                .build()
                .perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isOk());
    }
}
