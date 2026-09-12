package com.navi.net_pay_backend.infrastructure.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF ya que usamos tokens JWT y no cookies
                .csrf(csrf -> csrf.disable())
                // 2. Configurar las reglas de acceso a las rutas
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso totalmente público al login sin importar qué
                        .requestMatchers("/api/auth/login").permitAll()
                        // Cualquier otra ruta del backend requerirá que el usuario esté autenticado
                        .anyRequest().authenticated()
                )
                // 3. Apagar las sesiones en memoria (Tu API ahora es Stateless gracias al JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}