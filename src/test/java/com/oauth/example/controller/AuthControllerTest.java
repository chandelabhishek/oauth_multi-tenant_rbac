package com.oauth.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.oauth.example.domain.dto.*;
import com.oauth.example.domain.validators.PhoneNumberValidator;
import com.oauth.example.service.AuthService;
import com.oauth.example.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AuthController.class, PhoneNumberValidator.class})
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebAppConfiguration
@EnableWebMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest {
    @Autowired
    private AuthController authController;

    @Autowired
    private PhoneNumberValidator phoneNumberValidator;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userServiceImpl;

    @MockBean
    private PhoneNumberUtil phoneNumberUtil;

    @Autowired
    private WebApplicationContext context;

    @BeforeAll
    public void setup() {

    }

    /**
     * Method under test: {@link AuthController#triggerForgotPasswordFlow(ForgotPasswordRequest)}
     */
    @Test
    void test_triggerForgotPasswordFlow() throws Exception {
        doNothing().when(authService).triggerForgotPasswordFlow(Mockito.any());

        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("jane.doe@example.org");
        String content = (new ObjectMapper()).writeValueAsString(forgotPasswordRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link AuthController#recoverPassword(RecoverPasswordRequest)}
     */
    @Test
    void test_recoverPassword() throws Exception {
        doNothing().when(authService).recoverPassword(Mockito.any());

        RecoverPasswordRequest recoverPasswordRequest = new RecoverPasswordRequest();
        recoverPasswordRequest.setNewPassword("password");
        recoverPasswordRequest.setToken("ABC123");
        String content = (new ObjectMapper()).writeValueAsString(recoverPasswordRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link AuthController#acceptUserInvite(AcceptUserInviteDto)}
     */
    @Test
    void test_acceptUserInvite() throws Exception {
        doNothing().when(userServiceImpl).acceptUserInvite(Mockito.any());

        AcceptUserInviteDto acceptUserInviteDto = new AcceptUserInviteDto();
        acceptUserInviteDto.setToken("ABC123");
        String content = (new ObjectMapper()).writeValueAsString(acceptUserInviteDto);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth/accept-user-invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController).build().perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link AuthController#login(LoginRequest)}
     */
    @Test
    void test_login_throws_400() throws Exception {
        MockHttpServletRequestBuilder contentTypeResult = MockMvcRequestBuilders.post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content(objectMapper.writeValueAsString(new LoginRequest()));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    /**
     * Method under test: {@link AuthController#login(LoginRequest)}
     */
    @Test
    @WithMockUser(username = "email@gmail.com", password = "password")
    void test_Login_success() throws Exception {
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/auth/login");
        MockHttpServletRequestBuilder contentTypeResult = postResult.contentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        var loginRequest = new LoginRequest("email@gmail.com", "password");
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content(objectMapper.writeValueAsString(loginRequest));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(authController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Method under test: {@link AuthController#register(SignUpRequest)}
     */
    @Test
    void test_register_400() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setCountryCode("GB");
        signUpRequest.setEmail("jane.doe@example.org");
        signUpRequest.setFirstName("j");
        signUpRequest.setLastName("Doe");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("+916625550144");
        String content = (new ObjectMapper()).writeValueAsString(signUpRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        ResultActions actualPerformResult = MockMvcBuilders
                .webAppContextSetup(context)
                .build()
                .perform(requestBuilder).andDo(MockMvcResultHandlers.print());
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void test_register_success() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setCountryCode("GB");
        signUpRequest.setEmail("jane.doe@example.org");
        signUpRequest.setFirstName("Jane");
        signUpRequest.setLastName("Doe");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("+917425399456");
        String content = (new ObjectMapper()).writeValueAsString(signUpRequest);
        SignupResponse signupResponse = new SignupResponse("some_access_token");
        doReturn(true).when(phoneNumberUtil).isValidNumber(Mockito.any());
        when(authService.register(signUpRequest)).thenReturn(signupResponse);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);


        ResultActions actualPerformResult = MockMvcBuilders
                .webAppContextSetup(context)
                .build()
                .perform(requestBuilder).andDo(MockMvcResultHandlers.print());
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isOk());
    }
}

