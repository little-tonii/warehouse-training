package com.training.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.training.warehouse.filter.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationEntryPoint authenticationEntryPoint;
        private final AccessDeniedHandler accessDeniedHandler;

        @Bean
        protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable());
                http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                http.authorizeHttpRequests(auth -> auth
                        // swagger docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        // auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/auth/me"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me"
                        ).authenticated()
                        // inbound
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/inbound/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inbound",
                                "/api/inbound/import-data"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/inbound/inventory",
                                "/api/inbound/*",
                                "/api/inbound",
                                "/api/inbound/*/attachment/*/download-url"
                        ).authenticated()
                        // outbound
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/outbound/*/confirm",
                                "/api/outbound/late-statistics"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/outbound"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/outbound/*"
                        ).authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/outbound/*"
                        ).authenticated()
                        // others
                        .anyRequest().denyAll());
                http.exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
