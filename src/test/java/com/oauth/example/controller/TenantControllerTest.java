package com.oauth.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.example.domain.dto.TenantDto;
import com.oauth.example.domain.dto.UserDto;
import com.oauth.example.domain.model.TenantConfig;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.TenantService;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TenantController.class})
@ExtendWith(SpringExtension.class)
class TenantControllerTest {
    @Autowired
    private TenantController tenantController;

    @MockBean
    private TenantService tenantService;

//    /**
//     * Method under test: {@link TenantController#createTenant(UserPrincipal, Tenant)}
//     */
//    @Test
//    void test_createTenant() throws Exception {
//        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/tenants");
//        MockHttpServletRequestBuilder contentTypeResult = postResult.param("userPrincipal", String.valueOf((Object) null))
//                .contentType(MediaType.APPLICATION_JSON);
//
//        Tenant tenant = TestUtils.getTenant();
//        tenant.setCreatedAt(null);
//        tenant.setUpdatedAt(null);
//        tenant.setDeletedAt(null);
//
//        var tenantWithToken = new TenantWithToken();
//        tenantWithToken.setTenant(tenant);
//        tenantWithToken.setAccessToken("some_access_token");
//
//        doReturn(tenantWithToken).when(tenantService).createTenant(Mockito.any());
//
//        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
//                .content((new ObjectMapper()).writeValueAsString(tenant));
//        MockMvcBuilders.standaloneSetup(tenantController)
//                .build()
//                .perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//    }

    /**
     * Method under test: {@link TenantController#getTenant(UserPrincipal)}
     */
    @Test
    void test_getTenant() throws Exception {
        var tenant = TestUtils.getTenant();
        var user = TestUtils.getUser(tenant);
        var userPrincipal = new UserPrincipal(user);


        doReturn(tenant).when(tenantService).getTenant(Mockito.any());
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.get("/v1/tenants");
        MockHttpServletRequestBuilder requestBuilder = postResult.param("userPrincipal", String.valueOf(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON);

        MockMvcBuilders.standaloneSetup(tenantController)
                .build()
                .perform(requestBuilder)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Method under test: {@link TenantController#addTenantUser(UserPrincipal, UserDto)}
     */
    @Test
    void test_addTenantUser() throws Exception {
        doNothing().when(tenantService).addTenantUser(Mockito.any(), Mockito.<UserPrincipal>any());

        UserDto userDto = new UserDto();
        userDto.setCountryCode("GB");
        userDto.setEmail("jane.doe@example.org");
        userDto.setFirstName("Jane");
        userDto.setLastName("Doe");
        userDto.setPassword("password");
        userDto.setPhoneNumber("6625550144");
        userDto.setSecureCode("Secure Code");
        String content = (new ObjectMapper()).writeValueAsString(userDto);
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/tenants/add-user");
        MockHttpServletRequestBuilder requestBuilder = postResult.param("userPrincipal", String.valueOf((Object) null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(tenantController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Method under test: {@link TenantController#blockUserMapping(UserPrincipal)}
     */
    @Test
    void test_blockUserMapping() throws Exception {
        doNothing().when(tenantService).blockUserFromTenant(Mockito.any());
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/tenants/users/42/block");
        MockHttpServletRequestBuilder requestBuilder = postResult.param("userPrincipal", String.valueOf((Object) null));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(tenantController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link TenantController#deleteUserMapping(UserPrincipal)}
     */
    @Test
    void test_deleteUserMapping() throws Exception {
        doNothing().when(tenantService).deleteUserFromTenant(Mockito.any());
        MockHttpServletRequestBuilder deleteResult = MockMvcRequestBuilders.delete("/v1/tenants/users/42");
        MockHttpServletRequestBuilder requestBuilder = deleteResult.param("userPrincipal", String.valueOf((Object) null));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(tenantController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link TenantController#getTenantUsers(UserPrincipal)}
     */
    @Test
    void test_getTenantUsers() throws Exception {
        when(tenantService.getUsers(Mockito.any())).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder getResult = MockMvcRequestBuilders.get("/v1/tenants/users");
        MockHttpServletRequestBuilder requestBuilder = getResult.param("userPrincipal", String.valueOf((Object) null));
        MockMvcBuilders.standaloneSetup(tenantController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("{\"data\":[],\"message\":null}"));
    }

    /**
     * Method under test: {@link TenantController#updateTenant(TenantDto)}
     */
    @Test
    void test_updateTenant() throws Exception {
        MockHttpServletRequestBuilder contentTypeResult = MockMvcRequestBuilders.put("/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON);

        TenantDto tenantDTO = new TenantDto();
        tenantDTO.setAddress("42 Main St");
        tenantDTO.setClientId("42");
        tenantDTO.setConfig(new TenantConfig());
        tenantDTO.setCountry("GB");
        tenantDTO.setCountryCode("GB");
        tenantDTO.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
        tenantDTO.setEmail("jane.doe@example.org");
        tenantDTO.setHasOwnCredentials(true);
        tenantDTO.setId(UUID.randomUUID());
        tenantDTO.setName("Name");
        tenantDTO.setPhoneNumber("6625550144");
        tenantDTO.setState("MD");
        tenantDTO.setStatus("Status");
        tenantDTO.setUpdatedBy("2020-03-01");
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content((new ObjectMapper()).writeValueAsString(tenantDTO));
        MockMvcBuilders.standaloneSetup(tenantController).build().perform(requestBuilder).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }
}

