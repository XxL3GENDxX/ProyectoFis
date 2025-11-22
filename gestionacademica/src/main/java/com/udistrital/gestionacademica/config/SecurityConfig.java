package com.udistrital.gestionacademica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF para llamadas fetch desde JS (ajusta según necesites)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // permitir acceso público a la API REST y a recursos estáticos
                        .requestMatchers("/api/**", "/css/**", "/js/**", "/images/**", "/gestionarEstudiantes.html", "/", "/index.html").permitAll()
                        // cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                // dejamos el httpBasic por defecto (no obligatorio si todo está en permitAll)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
