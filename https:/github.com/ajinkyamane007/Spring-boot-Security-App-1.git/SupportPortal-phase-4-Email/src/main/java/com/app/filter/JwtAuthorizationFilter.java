package com.app.filter;

import com.app.constant.SecurityConstant;
import com.app.utility.JWTTokenProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

//import static com.app.constant.SecurityConstant.*;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private JWTTokenProvider jwtTokenProvider;

    public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
    	if (request.getMethod().equalsIgnoreCase(SecurityConstant.OPTIONS_HTTP_METHOD))
    	{
            response.setStatus(HttpStatus.OK.value());
        } 
    	else
    	{
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstant.TOKEN_PREFIX))
            {
                filterChain.doFilter(request, response);
                return;
            }
            // JWT Token is in the form "Bearer token". 
            // Remove Bearer word and get only the Token
            String token = authorizationHeader.substring(SecurityConstant.TOKEN_PREFIX.length());
            String username = jwtTokenProvider.getSubject(token);
    		
            // Once we get the token validate it.
            if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) 
            {
                List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
             // After setting the Authentication in the context, we specify
             // that the current user is authenticated. So it passes the
             // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else 
            {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
