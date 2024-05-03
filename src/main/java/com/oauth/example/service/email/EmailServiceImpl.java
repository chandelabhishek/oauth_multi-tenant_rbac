package com.oauth.example.service.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oauth.example.config.SendgridConfig;
import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LogManager.getLogger(EmailServiceImpl.class);
    @Autowired
    private SendgridConfig sendgridConfig;

    @Autowired
    private SendGrid sendGrid;

    private void sendEmail(Mail mail) throws IOException {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (IOException ex) {
            logger.error(ex);
            throw ex;
        }
    }

    private Mail buildEmail(String subject, String to, HashMap<String, Object> contextData, String templateId) {
        DynamicTemplatePersonalization personalization = new DynamicTemplatePersonalization();
        personalization.addTo(new Email(to));
        contextData.forEach(personalization::addDynamicTemplateData);
        Mail mail = new Mail();
        mail.setFrom(new Email(sendgridConfig.getSenderEmail(), sendgridConfig.getSenderName()));
        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId);
        mail.setSubject(subject);
        return mail;
    }

    @Override
    @Async
    public void sendForgotPasswordEmail(String recoveryLink, User user) throws IOException {
        String subject = "Reset Your Password!!";
        var contextData = new HashMap<String, Object>();
        contextData.put("name", user.getFirstName());
        contextData.put("resetUrl", recoveryLink);
        sendEmail(buildEmail(subject, user.getEmail(), contextData, sendgridConfig.getForgotPasswordTemplateId()));
    }

    @Override
    @Async
    public void sendUserInviteEmail(User user, String userInviteLink, Tenant tenant) {
        try {
            String subject = "Congratulations !! you have been invited";
            var contextData = new HashMap<String, Object>();
            contextData.put("name", user.getFirstName());
            contextData.put("tenantName", tenant.getName());
            contextData.put("userInviteLink", userInviteLink);
            sendEmail(buildEmail(subject, user.getEmail(), contextData, sendgridConfig.getUserInviteTemplateId()));
            logger.info("email sent to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private static class DynamicTemplatePersonalization extends Personalization {

        @JsonProperty(value = "dynamic_template_data")
        private Map<String, Object> dynamicTemplateData;

        @Override
        @JsonProperty("dynamic_template_data")
        public Map<String, Object> getDynamicTemplateData() {
            return Objects.requireNonNullElse(dynamicTemplateData, Collections.emptyMap());
        }

        @Override
        public void addDynamicTemplateData(String key, Object value) {
            if (dynamicTemplateData == null) {
                dynamicTemplateData = new HashMap<>();
                dynamicTemplateData.put(key, value);
            } else {
                dynamicTemplateData.put(key, value);
            }
        }
    }
}
