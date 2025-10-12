package com.example.carebloom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private RoleAuthenticationFilter roleAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

           .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(roleAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll() // All public endpoints
                .requestMatchers("/api/v1/mothers/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/auth/**").permitAll()
                .requestMatchers("/api/v1/midwife/auth/**").permitAll()
                .requestMatchers("/api/v1/vendor/auth/**").permitAll()
                .requestMatchers("/api/v1/moh/auth/**").permitAll()
                .requestMatchers("/api/clinics/*/queue/events").permitAll() // Allow SSE endpoint
                .requestMatchers("/api/v1/moh/clinics/*/queue/stream").permitAll() // Allow MoH SSE endpoint
                .requestMatchers("/api/v1/clinics/*/queue/**").permitAll() // Allow direct clinic queue endpoints for testing
                .requestMatchers("/api/v1/moh/clinics/*/queue/**").permitAll() // Allow MOH queue endpoints for testing
                .requestMatchers("/api/v1/mothers/**").hasRole("MOTHER")
                .requestMatchers("/api/v1/hints/**").permitAll()
                .requestMatchers("/api/v1/hints").permitAll()
                .requestMatchers("/api/v1/articles/**").permitAll()
                .requestMatchers("/api/v1/articles").permitAll()
                .requestMatchers("/api/v1/debug/**").permitAll() // Allow debug endpoints for testing
               // .requestMatchers("/api/v1/test/**").permitAll() // Allow unauthenticated access for testing
                .requestMatchers("/api/v1/mother/**").hasRole("MOTHER")
                .requestMatchers("/api/v1/admin/**").hasRole("PLATFORM_MANAGER")
                .requestMatchers("/api/v1/midwife/**").hasRole("MIDWIFE")
                .requestMatchers("/api/v1/vendor/**").hasRole("VENDOR")
                .requestMatchers("/api/v1/moh/**").hasAnyRole("MOH_OFFICE_USER", "MOH_OFFICE_ADMIN")
                .anyRequest().authenticated()
            );
    
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(roleAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/api/v1/public/**").permitAll() // All public endpoints
                        .requestMatchers("/api/v1/mothers/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/auth/**").permitAll()
                        .requestMatchers("/api/v1/midwife/auth/**").permitAll()
                        .requestMatchers("/api/v1/vendor/auth/**").permitAll()
                        .requestMatchers("/api/v1/moh/auth/**").permitAll()
                        .requestMatchers("/api/v1/mothers/**").hasRole("MOTHER")
                        // .requestMatchers("/api/v1/test/**").permitAll() // Allow unauthenticated
                        // access for testing
                        .requestMatchers("/api/v1/mother/**").hasRole("MOTHER")
                        .requestMatchers("/api/v1/admin/**").hasRole("PLATFORM_MANAGER")
                        .requestMatchers("/api/v1/midwife/**").hasRole("MIDWIFE")
                        .requestMatchers("/api/v1/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/api/v1/moh/**").hasAnyRole("MOH_OFFICE_USER", "MOH_OFFICE_ADMIN")
                        .anyRequest().authenticated());

        return http.build();
    }
}