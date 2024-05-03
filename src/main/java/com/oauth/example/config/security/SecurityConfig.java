package com.oauth.example.config.security;

import com.oauth.example.domain.enums.Authority;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    static final String[] openUrls = {
            "/v1/auth/login",
            "/oauth2/token",
            "/login",
            "/oauth2/login",
            "/v1/auth/register",
            "/v1/auth/forgot-password",
            "/v1/auth/recover-password",
            "/v1/auth/accept-user-invite",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/swagger-ui/index.html",
            // admin
            "/v1/admin/login",
            "/actuator/**"
    };
    static final String[] adminRestrictedUrls = {
            "/v1/admin/create-tenant",
            "/tenant/{tenantId}/add-user",
            "/create-user",
            "/tenant/{tenantId}/add-user/{userId}"
    };
    static final String[] allPossibleAuthorities = {
            Authority.SCOPE_TENANT_NOT_AVAILABLE.name(),
            Authority.SCOPE_TENANT_ADMIN.name(),
            Authority.SCOPE_AGENCY_ADMIN.name(),
            Authority.SCOPE_SYSTEM_ADMIN.name()
    };

    static final String[] allAdminAuthorities = {
            Authority.SCOPE_TENANT_ADMIN.name(),
            Authority.SCOPE_AGENCY_ADMIN.name(),
            Authority.SCOPE_SYSTEM_ADMIN.name()
    };
    private static final Logger logger = LogManager.getLogger(SecurityConfig.class);

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   LogoutHandler logoutHandler,
                                                   ExceptionHandlerFilter exceptionHandlerFilter
    ) throws Exception {
        http.authorizeHttpRequests(registry -> registry
                .requestMatchers("/oauth2/token").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers(openUrls).permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/tenants/assign-tenant")
                .hasAnyAuthority(allPossibleAuthorities)
                .requestMatchers(HttpMethod.GET, "/v1/auth/get-tenants")
                .hasAnyAuthority(allPossibleAuthorities)
                .requestMatchers(HttpMethod.POST, "/v1/tenants/create-agency")
                .hasAnyAuthority(Authority.SCOPE_SYSTEM_ADMIN.name(), Authority.SCOPE_TENANT_ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/v1/tenants/add-user")
                .hasAnyAuthority(allPossibleAuthorities)
                .requestMatchers("/v1/user/**")
                .hasAnyAuthority(allPossibleAuthorities)
                .requestMatchers(adminRestrictedUrls)
                .hasAuthority(Authority.SCOPE_SYSTEM_ADMIN.name())
                .requestMatchers("/v1/api-keys/**")
                .hasAnyAuthority(allAdminAuthorities)
                .anyRequest()
                .authenticated()
        );
        http.authenticationProvider(authenticationProvider);
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(Customizer.withDefaults());
//        http.oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer ->
//                httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults())
//        );
        // Set session management to stateless
        http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(exceptionHandlerFilter, LogoutFilter.class);
        http.exceptionHandling(
                exceptions ->
                        exceptions
                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    throw accessDeniedException;
                                })
        );
        http.cors(Customizer.withDefaults());

        http.logout(logout ->
                logout
                        .logoutUrl("/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        );
        return http.build();
    }
}

