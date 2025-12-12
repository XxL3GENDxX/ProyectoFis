package com.udistrital.gestionacademica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilitar CORS
                .cors(Customizer.withDefaults())
                // Desactivar CSRF para llamadas fetch desde JS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                // Permitir acceso público a toda la API y recursos estáticos
                .requestMatchers(
                        "/api/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/**"
                ).permitAll()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
                )
                // HTTP Basic por defecto
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir todos los orígenes en desarrollo
        configuration.setAllowedOrigins(Arrays.asList(
                "*"  // En producción, cambiar a orígenes específicos
        ));

        // Métodos permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciales (solo si no usas "*" para orígenes)
        configuration.setAllowCredentials(false);

        // Exponer headers
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Tiempo de cache para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
