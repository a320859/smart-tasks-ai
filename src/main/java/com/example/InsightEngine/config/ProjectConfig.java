package com.example.InsightEngine.config;

import com.example.InsightEngine.auth.JwtAuthenticationFilter;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableFeignClients("com.example.InsightEngine")
public class ProjectConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    public ProjectConfig(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(DataSource dataSource) {
        var userDetailsManager =  new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setAuthoritiesByUsernameQuery("SELECT username, authority FROM mydb.authorities WHERE username = ?");
        userDetailsManager.setUsersByUsernameQuery("SELECT username, password, enabled FROM mydb.users WHERE username = ?");
        return userDetailsManager;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(c -> c.requestMatchers("/auth/**").permitAll().anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
      return authenticationConfiguration.getAuthenticationManager();
    }
}
