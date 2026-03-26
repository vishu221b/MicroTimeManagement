package com.microtimemanagement.apiservice.config;

import com.microtimemanagement.apiservice.filter.MtmSessionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MtmSessionFilter mtmSessionFilter;

    @Bean
    public static GrantedAuthorityDefaults authorityDefaults(){
        return new GrantedAuthorityDefaults("_");
    }

    @Bean
    @Profile("dev")
    CorsConfigurationSource corsConfigurationDevSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setMaxAge(Duration.ofHours(12));
        configuration.setAllowedMethods(List.of(
                HttpMethod.HEAD.name(),
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name()
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
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name()
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
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer
                                .configurationSource(corsConfigurationDevSource())
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/api/v1/auth/login",
                                        "/actuator/**",
                                        "/error",
                                        "/api/v1/users/register",
                                        "/",
                                        "/swagger-dev"
                                ).permitAll()
                                .requestMatchers("/api/v1/admin/**").hasRole("MTM_ADMIN")
                                .requestMatchers("/api/v1/**", "/api/v1/auth/logout").hasRole("MTM_USER")
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
