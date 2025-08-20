package com.stockmarketproject.config;

import com.stockmarketproject.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService uds;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/auth/register")
        );
        http.headers(h -> h.frameOptions(f -> f.disable())); // H2 console için

        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(auth -> auth
                // thymeleaf sayfaları + statikler serbest
                .requestMatchers("/", "/login", "/register", "/stocks",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/webjars/**", "/h2-console/**").permitAll()
                // Kayıt endpoint'i serbest olmalı
                .requestMatchers("/api/auth/register").permitAll()
                // Diğer her şey Basic Auth ile
                .anyRequest().authenticated()
        );

        http.httpBasic(Customizer.withDefaults());
        http.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login"));

        return http.build();
    }
}
