package com.example.InsightEngine.services;

import com.example.InsightEngine.auth.JwtUtil;
import com.example.InsightEngine.dto.TokenDTO;
import com.example.InsightEngine.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class LoginService {
    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> login(UserDTO userDTO) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword(), Collections.emptyList());
        try {
            authenticationManager.authenticate(authentication);
            return ResponseEntity.ok(new TokenDTO(new JwtUtil().generateToken(userDTO.getUsername())));
        } catch (AuthenticationException authenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
