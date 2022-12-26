package com.microtimemanagement.apiservice.filter;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.ValidSessionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementAuthenticationException;
import com.microtimemanagement.apiservice.handler.MicroTimeManagementResourceExceptionHandler;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.AuthService;
import com.microtimemanagement.apiservice.service.UserService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtmSessionFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    MicroTimeManagementResourceExceptionHandler microTimeManagementResourceExceptionHandler;

    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String error = null;
        Integer status;
        try{
        final String authHeader = request.getHeader("Authorization");
        final String token;

        if(null!=authHeader && authHeader.startsWith("Bearer")){

            token = authHeader.substring(7);

            if(null == SecurityContextHolder.getContext().getAuthentication()){

                ValidSessionDTO validSessionDTO = authService.isValidSessionToken(token);

                if(!validSessionDTO.getIsValidSession()){
                    error=validSessionDTO.getError();
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
        }catch (MicroTimeManagementAuthenticationException e){
            handlerExceptionResolver.resolveException(request, response, microTimeManagementResourceExceptionHandler, e);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(error);
        }
    }
}
