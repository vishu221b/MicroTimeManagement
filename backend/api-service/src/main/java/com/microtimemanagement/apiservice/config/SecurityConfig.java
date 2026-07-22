package com.microtimemanagement.apiservice.config;

import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.constants.SecurityConstants;
import com.microtimemanagement.apiservice.filter.MtmSessionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MtmSessionFilter mtmSessionFilter;

    @Value("${mtm.cors.origins:http://localhost:3000,http://localhost:8080}")
    private String[] devAllowedOrigins;

    @Bean
    @Profile("dev")
    CorsConfigurationSource corsConfigurationDevSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOriginPatterns(Arrays.asList(devAllowedOrigins));
        configuration.setMaxAge(Duration.ofHours(12));
        configuration.setAllowedMethods(List.of(
                HttpMethod.HEAD.name(),
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Profile("prod")
    CorsConfigurationSource corsConfigurationProdSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOriginPatterns(List.of("https://mtm.app"));
        configuration.setMaxAge(Duration.ofHours(12));
        configuration.setAllowedMethods(List.of(
                HttpMethod.HEAD.name(),
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    @Profile("dev")
    SecurityFilterChain devSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationDevSource()))
                .authorizeHttpRequests(
                        auth -> auth
                                // Liveness/readiness probe — open so orchestrators
                                // (docker compose, k8s) can health-check without creds.
                                .requestMatchers("/actuator/health", "/actuator/health/**")
                                .permitAll()

                                .requestMatchers(SecurityConstants.DEV.OPEN_API_ENDPOINT_REQUEST_MATCHERS)
                                .permitAll()

                                .requestMatchers(SecurityConstants.DEV.SECURE_ADMIN_API_ENDPOINT_REQUEST_MATCHERS)
                                .hasRole(RoleConstants.ADMIN_OPS_ROLE)

                                .requestMatchers(SecurityConstants.DEV.SECURE_USER_API_ENDPOINT_REQUEST_MATCHERS)
                                .hasRole(RoleConstants.USER_OPS_ROLE)

                                .requestMatchers(SecurityConstants.DEV.SECURE_ACTIVITY_API_ENDPOINT_REQUEST_MATCHERS)
                                .hasRole(RoleConstants.ACTIVITY_CRUD)

                                .requestMatchers(SecurityConstants.DEV.SECURE_ROLE_API_ENDPOINT_REQUEST_MATCHERS)
                                .hasRole(RoleConstants.ROLE_CRUD)

                                .anyRequest().denyAll()
                )
                .addFilterBefore(mtmSessionFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Profile("prod")
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors->cors.configurationSource(corsConfigurationProdSource()))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/api/v1/users/register",
                                        "/api/v1/auth/login",
                                        "/error"
                                ).permitAll()
                                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                                .requestMatchers("/", "/actuator/**").hasRole("MTM_ACTUATOR")
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("MTM_SWAGGER")
                                .requestMatchers("/api/v1/admin/**").hasRole("MTM_ADMIN")
                                .requestMatchers("/api/v1/**", "api/v1/auth/logout").hasRole("MTM_USER")
                                .anyRequest().denyAll()
                )
                .addFilterBefore(mtmSessionFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
