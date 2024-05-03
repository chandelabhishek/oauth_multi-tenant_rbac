package com.oauth.example.controller;

import com.oauth.example.domain.dto.ApiKeysDto;
import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.model.UserPrincipal;
import com.oauth.example.service.ApiKeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ApiKeyController.class, UserPrincipal.class})
@ExtendWith(SpringExtension.class)
class ApiKeyControllerTest {
    @Autowired
    private ApiKeyController apiKeyController;

    @MockBean
    private ApiKeyService apiKeyService;

    @Autowired
    private UserPrincipal userPrincipal;

    /**
     * Method under test: {@link ApiKeyController#createApiKeys(UserPrincipal)}
     */
    @Test
    void test_createApiKeys() throws Exception {
        // Arrange
        ApiKeysDto buildResult = ApiKeysDto.builder().clientId("42").clientSecret("Client Secret").build();
        when(apiKeyService.createApiKeys(Mockito.any())).thenReturn(buildResult);
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/v1/api-keys");
        MockHttpServletRequestBuilder requestBuilder = postResult.param("userPrincipal", String.valueOf(userPrincipal));

        // Act
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(apiKeyController)
                .build()
                .perform(requestBuilder);

        // Assert
        actualPerformResult.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"data\":{\"clientId\":\"42\",\"clientSecret\":\"Client Secret\"},\"message\":null}"));
    }
}
