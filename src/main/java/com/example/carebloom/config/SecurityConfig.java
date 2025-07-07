package com.example.carebloom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
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
                .requestMatchers("/api/v1/mother/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/auth/**").permitAll()
                .requestMatchers("/api/v1/midwife/auth/**").permitAll()
                .requestMatchers("/api/v1/vendor/auth/**").permitAll()
                .requestMatchers("/api/v1/moh/auth/**").permitAll()
               // .requestMatchers("/api/v1/test/**").permitAll() // Allow unauthenticated access for testing
                .requestMatchers("/api/v1/mother/**").hasRole("MOTHER")
                .requestMatchers("/api/v1/admin/**").hasRole("PLATFORM_MANAGER")
                .requestMatchers("/api/v1/midwife/**").hasRole("MIDWIFE")
                .requestMatchers("/api/v1/vendor/**").hasRole("VENDOR")
                .requestMatchers("/api/v1/moh/**").hasRole("MOH_OFFICE")
                .anyRequest().authenticated()
            );
    
        return http.build();
    }
}