package com.stockmarketproject.config;

import com.stockmarketproject.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
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
        http

                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/webjars/**", "/h2-console/**",
                                "/api/auth/register"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, ex0) -> {
                    if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        res.setHeader("WWW-Authenticate", "Basic realm=\"stock\"");
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }))

                .httpBasic(Customizer.withDefaults())
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login"));

        return http.build();
    }
}
