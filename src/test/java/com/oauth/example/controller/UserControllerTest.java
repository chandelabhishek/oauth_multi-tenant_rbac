package com.oauth.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.example.domain.dto.PasswordChangeRequest;
import com.oauth.example.domain.dto.UserMeDto;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.UserService;
import com.oauth.example.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserController.class})
@ExtendWith(SpringExtension.class)
class UserControllerTest {
    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    /**
     * Method under test: {@link UserController#me(UserPrincipal)}
     */
    @Test
    void test_me() throws Exception {
        var tenant = TestUtils.getTenant();
        var user = TestUtils.getUser(tenant);
        var userPrincipal = new UserPrincipal(user);
        var userMeDto = new UserMeDto();
        userMeDto.setTenant(tenant);
        userMeDto.setEmail(user.getEmail());
        when(userService.me(Mockito.any())).thenReturn(userMeDto);

        MockHttpServletRequestBuilder getResult = MockMvcRequestBuilders.get("/v1/user/me");
        MockHttpServletRequestBuilder requestBuilder = getResult.param("userPrincipal", String.valueOf(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON);
        MockMvcBuilders.standaloneSetup(userController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(user.getEmail()));
    }

    /**
     * Method under test: {@link UserController#changePassword(UserPrincipal, PasswordChangeRequest)}
     */
    @Test
    void test_changePassword() throws Exception {
        doNothing().when(userService).changePassword(Mockito.any(), Mockito.any());

        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setNewPassword("password1");
        passwordChangeRequest.setOldPassword("password2");
        String content = (new ObjectMapper()).writeValueAsString(passwordChangeRequest);
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/user/change-password");
        MockHttpServletRequestBuilder requestBuilder = postResult.param("userPrincipal", String.valueOf((Object) null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(userController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

