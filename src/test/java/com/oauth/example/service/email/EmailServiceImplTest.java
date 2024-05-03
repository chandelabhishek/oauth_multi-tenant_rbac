package com.oauth.example.service.email;

import com.oauth.example.config.SendgridConfig;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.oauth.example.utils.TestUtils;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {EmailServiceImpl.class, SendgridConfig.class})
@ExtendWith(SpringExtension.class)
class EmailServiceImplTest {
    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @MockBean
    private SendGrid sendGrid;

    /**
     * Method under test: {@link EmailServiceImpl#sendForgotPasswordEmail(String, User)}
     */
    @Test
    void test_sendForgotPasswordEmail_success() throws IOException {
        Tenant currentTenant = TestUtils.getTenant();
        User user = TestUtils.getUser(currentTenant);
        Request request = mock();
        doNothing().when(request).setBody(Mockito.any());
        doNothing().when(request).setEndpoint(Mockito.any());
        doNothing().when(request).setMethod(Mockito.any());

        when(sendGrid.api(Mockito.any())).thenReturn(new Response());
        emailServiceImpl.sendForgotPasswordEmail("Recovery Link", user);
        verify(sendGrid).api(Mockito.any());
    }

    /**
     * Method under test: {@link EmailServiceImpl#sendUserInviteEmail(User, String, Tenant)}
     */
    @Test
    void test_sendUserInviteEmail_success() throws IOException {
        Tenant currentTenant = TestUtils.getTenant();
        User user = TestUtils.getUser(currentTenant);
        Request request = mock();
        Response response = new Response();
        when(sendGrid.api(Mockito.any())).thenReturn(response);
        doNothing().when(request).setBody(Mockito.any());
        doNothing().when(request).setEndpoint(Mockito.any());
        doNothing().when(request).setMethod(Mockito.any());
        response.setStatusCode(200);
        emailServiceImpl.sendUserInviteEmail(user, "User Invite Link", currentTenant);
        verify(sendGrid).api(Mockito.any());
    }
}

