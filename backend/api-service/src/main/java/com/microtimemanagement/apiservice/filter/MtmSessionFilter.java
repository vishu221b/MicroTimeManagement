package com.microtimemanagement.apiservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microtimemanagement.apiservice.dto.ExceptionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementAuthenticationException;
import com.microtimemanagement.apiservice.service.AuthService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtmSessionFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final AuthService authService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try{
            final String authHeader = request.getHeader("Authorization");
            final String token;

            if(null!=authHeader && authHeader.startsWith("Bearer")){

                token = authHeader.substring(7);

                if(null == SecurityContextHolder.getContext().getAuthentication()){

                    ValidSessionDTO validSessionDTO = authService.isValidSessionToken(token);

                    if(!validSessionDTO.getIsValidSession()){
                        throw new MicroTimeManagementAuthenticationException(validSessionDTO.getError());
                    }
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    validSessionDTO.getPrincipal(),
                                    null,
                                    validSessionDTO.getPrincipal().getAuthorities()
                            );
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                }
            }
            filterChain.doFilter(request, response);
        }catch (Exception e){
            e.printStackTrace();
            Map<String, String> errors = new HashMap<>();
            errors.put("message", e.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ExceptionDTO.builder()
                                    .error(errors)
                                    .path(request.getServletPath())
                                    .statusCode(HttpStatus.FORBIDDEN.value())
                                    .code(System.currentTimeMillis())
                                    .build()
                    ));
        }
    }
}
