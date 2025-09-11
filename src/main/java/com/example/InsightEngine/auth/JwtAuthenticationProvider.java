package com.example.InsightEngine.auth;

import com.example.InsightEngine.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationProvider (UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        var user = userDetailsService.loadUserByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
           return new UsernamePasswordAuthenticationToken(username, password);
        } else {
            throw new AuthenticationException("invalid credentials") {
                @Override
                public Authentication getAuthenticationRequest() {
                    return super.getAuthenticationRequest();
                }
            };
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BCryptPasswordEncoder.class.isAssignableFrom(authentication);
    }
}
