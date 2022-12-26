package com.microtimemanagement.apiservice.config;

import com.microtimemanagement.apiservice.filter.MtmSessionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    MtmSessionFilter mtmSessionFilter;
    public static final Integer BCRYPT_STRENGTH = 10; //default

    /**
     * For custom prefixing to all roles
     * */
    @Bean
    public static GrantedAuthorityDefaults authorityDefaults(){
        return new GrantedAuthorityDefaults("_");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic().disable().csrf().disable().cors()
                .and()
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/api/v1/auth/**",
                                        "/actuator/**",
                                        "/error",
                                        "/api/v1/users/register"
                                ).permitAll()
                                .requestMatchers("/api/v1/admin/**").hasRole("MTM_ADMIN")
                                .requestMatchers("/api/v1/**").hasRole("MTM_USER")
                ).authorizeHttpRequests().anyRequest().denyAll()
                .and().addFilterBefore(mtmSessionFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().build();
    }
}
