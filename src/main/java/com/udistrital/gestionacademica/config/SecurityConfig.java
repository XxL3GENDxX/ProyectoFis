package com.udistrital.gestionacademica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfigurationSource; // Importante
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Importante

import java.util.Arrays; // Importante

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivar CSRF para llamadas fetch desde JS (ajusta según necesites)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                // permitir acceso público a la API REST y a recursos estáticos
                .requestMatchers(HttpMethod.POST, "/api/**", "/css/**", "/js/**", "/images/**",
                        "/gestionarEstudiantes.html", "/**", "/index.html")
                .permitAll()
                // cualquier otra petición requiere autenticación
                .anyRequest().authenticated())
                // dejamos el httpBasic por defecto (no obligatorio si todo está en permitAll)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Permitir el origen de tu Frontend (Live Server usa el 5500)
        // IMPORTANTE: Pon tanto localhost como 127.0.0.1 por si acaso
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500", "http://localhost:5500"));

        // 2. Métodos permitidos (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. Headers permitidos (Content-Type, Authorization, etc.)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. Permitir credenciales (cookies, etc.) si fuera necesario
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
