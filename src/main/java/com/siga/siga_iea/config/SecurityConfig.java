package com.siga.siga_iea.config;

import com.siga.siga_iea.auth.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Mantener disabled o habilitar si es necesario
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/icons/**", "/fonts/**", "/favicon.ico").permitAll()
                        .requestMatchers("/matricula/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO")
                        .requestMatchers("/personal/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO")
                        .requestMatchers("/ambiental/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO")
                        .requestMatchers("/configuracion/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO")
                        .requestMatchers("/usuarios/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO")
                        .requestMatchers("/estudiantes/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE")
                        .requestMatchers("/clases/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE")
                        .requestMatchers("/asistencias/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE")
                        .requestMatchers("/reportes/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE", "ESTUDIANTE")
                        .requestMatchers("/calificaciones/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE", "ESTUDIANTE")
                        .requestMatchers("/certificados/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "ESTUDIANTE")
                        .requestMatchers("/soporte/**").hasAnyRole("ADMIN", "PERSONAL_ADMINISTRATIVO", "DOCENTE", "ESTUDIANTE")
                        .requestMatchers("/").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("siga-ieaci-secret-key-2026")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(86400 * 7) // 7 días
                        .userDetailsService(userDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        return http.build();
    }
}

