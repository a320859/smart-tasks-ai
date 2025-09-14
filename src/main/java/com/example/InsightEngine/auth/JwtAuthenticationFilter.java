package com.example.InsightEngine.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String header = request.getHeader("Authorization");
       if (header != null && header.startsWith("Bearer ")){
           String token = header.substring(7);
           try {
               String username = new JwtUtil().parseToken(token).getSubject();
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);
               SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList()));
           } catch (Exception e) {
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               return;
           }
        }
       filterChain.doFilter(request, response);
    }
}
