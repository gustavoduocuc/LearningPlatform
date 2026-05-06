package com.duoc.LearningPlatform.config;

import com.duoc.LearningPlatform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.duoc.LearningPlatform.repository.UserRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserRepository userRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/courses/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/courses/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/courses/{id}/activate").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/courses/{id}/deactivate").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/registrations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/registrations/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/registrations/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/payments").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/payments/{id}").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers(HttpMethod.PUT, "/api/payments/{id}/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/payments/{id}/cancel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/notifications").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/notifications/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/notifications").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/notifications/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/notifications/{id}/read").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/notifications/{id}").authenticated()
                // Student-specific evaluation endpoints (must be before generic /{id} patterns)
                .requestMatchers(HttpMethod.GET, "/api/evaluations/my-evaluations").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/evaluations/{id}/my-submission").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/evaluations/{id}/my-grade").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/evaluations/{id}/submissions").hasRole("STUDENT")
                // Admin/Professor evaluation endpoints
                .requestMatchers(HttpMethod.GET, "/api/evaluations").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/evaluations/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/evaluations").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/evaluations/{id}").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.DELETE, "/api/evaluations/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/evaluations/{id}/grades").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/evaluations/{id}/grades").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/evaluations/{id}/submissions").hasAnyRole("ADMIN", "PROFESSOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
