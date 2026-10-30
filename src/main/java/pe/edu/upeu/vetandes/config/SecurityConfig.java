package pe.edu.upeu.vetandes.config;

import pe.edu.upeu.vetandes.security.*;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwt,
            UsuarioDetailsService usuarios,
            RespuestaSeguridad errores) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errores)
                        .accessDeniedHandler(errores))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/v1/auth/login"
                        ).permitAll()

                        .requestMatchers("/api/v1/auth/me")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/propietarios")
                        .hasAuthority("MASCOTA_LEER")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/prestaciones",
                                "/api/v1/prestaciones/**")
                        .hasAuthority("PRESTACION_LEER")

                        .requestMatchers(
                                "/api/v1/prestaciones",
                                "/api/v1/prestaciones/**")
                        .hasAuthority("PRESTACION_ESCRIBIR")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/mascotas",
                                "/api/v1/mascotas/**")
                        .hasAuthority("MASCOTA_LEER")

                        .requestMatchers(
                                "/api/v1/mascotas",
                                "/api/v1/mascotas/**")
                        .hasAuthority("MASCOTA_ESCRIBIR")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/atenciones",
                                "/api/v1/atenciones/*")
                        .hasAuthority("ATENCION_LEER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/atenciones")
                        .hasAuthority("ATENCION_REGISTRAR")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/atenciones/*/anular")
                        .hasAuthority("ATENCION_ANULAR")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/reportes/atenciones-por-especie")
                        .hasAuthority("REPORTE_VER")

                        .anyRequest().denyAll()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwt, usuarios, errores),
                        UsernamePasswordAuthenticationFilter.class
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
}