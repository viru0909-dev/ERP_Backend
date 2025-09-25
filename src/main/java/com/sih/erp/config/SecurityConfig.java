package com.sih.erp.config;

import com.sih.erp.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired private UserDetailsServiceImpl userDetailsService;
// In src/main/java/com/sih/erp/config/SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC Endpoints (No login required) ---
                        .requestMatchers("/api/auth/**", "/api/public/**", "/api/master/**", "/api/files/**").permitAll()

                        // --- STUDENT Endpoints ---
                        .requestMatchers("/api/student/**").hasAuthority("ROLE_STUDENT")

                        // --- TEACHER Endpoints ---
                        .requestMatchers("/api/teacher/**").hasAuthority("ROLE_TEACHER")

                        // --- COURSE Endpoints (Shared between Student and Teacher) ---
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasAuthority("ROLE_TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAuthority("ROLE_TEACHER")

                        // --- STAFF Endpoints ---
                        .requestMatchers("/api/staff/admissions/**").hasAuthority("ROLE_ADMISSIONS_STAFF")
                        .requestMatchers("/api/hostel-staff/**").hasAuthority("ROLE_HOSTEL_ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ROLE_ACADEMIC_ADMIN", "ROLE_SUPER_STAFF")

                        // --- SUPER_STAFF ONLY Endpoints ---
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_SUPER_STAFF")

                        // --- GENERAL Authenticated Endpoints (Any logged-in user) ---
                        .requestMatchers("/api/users/me/**", "/api/timetable/class/**").authenticated()

                        // --- All other requests must be authenticated ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}