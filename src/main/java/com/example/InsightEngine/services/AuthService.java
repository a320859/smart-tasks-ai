package com.example.InsightEngine.services;

import com.example.InsightEngine.auth.JwtUtil;
import com.example.InsightEngine.dto.TokenDTO;
import com.example.InsightEngine.dto.UserDTO;
import com.example.InsightEngine.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> login(UserDTO userDTO) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getUsername(),
                userDTO.getPassword(), Collections.emptyList());
        try {
            authenticationManager.authenticate(authentication);
            return ResponseEntity.ok(new TokenDTO(new JwtUtil().generateToken(userDTO.getUsername())));
        } catch (AuthenticationException authenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    public ResponseEntity<?> register(UserDTO userDTO) {
        if ((userRepository.countOfUsersWithUsername(userDTO.getUsername()) == 0) &&
                (userDTO.getUsername().length() > 3 && userDTO.getPassword().length() > 3)) {
            userRepository.addUser(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()));
            userRepository.addUserAuthority(userDTO.getUsername());
            return ResponseEntity.ok("Registration successful");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
    }
}
