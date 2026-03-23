package com.makers.memoir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String issuer;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${app.base-url}")
    private String returnTo;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {



        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request -> {
            String accept = request.getHeader("Accept");
            return accept != null && accept.contains("text/html");
        });

        http
                .requestCache(cache -> cache.requestCache(requestCache))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/images/**", "/main.css").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            response.sendRedirect("/after-login");
                        })
                ).logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.sendRedirect(issuer + "v2/logout?returnTo="
                                    + returnTo + "&client_id=" + clientId);
                        })
                );

        return http.build();
    }
}