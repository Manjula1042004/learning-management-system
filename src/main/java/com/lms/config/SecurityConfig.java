package com.lms.config;

import com.lms.security.CustomUserDetailsService;
import com.lms.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF is disabled - this is correct for your setup
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                .authorizeHttpRequests(auth -> auth
                        // Swagger & API Docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Public endpoints
                        .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // ✅ CRITICAL FIX: Test endpoint for debugging
                        .requestMatchers("/lessons/test-controller", "/lessons/test-delete/**").permitAll()

                        // ✅ FIXED: Lesson delete - MORE SPECIFIC PATTERN FIRST
                        .requestMatchers("/lessons/delete/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers("/lessons/create/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers("/lessons/edit/**").hasAnyRole("INSTRUCTOR", "ADMIN")

                        // ✅ FIXED: Course management - SPECIFIC PATTERNS FIRST
                        .requestMatchers("/courses/create").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers("/courses/{id}/edit").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers("/courses/{id}/delete").hasAnyRole("INSTRUCTOR", "ADMIN")

                        // Courses viewing (less specific, comes after)
                        .requestMatchers("/courses", "/courses/view/**", "/courses/{id}").permitAll()

                        // Role-based access
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/instructor/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers("/student/**", "/enroll/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")

                        // ✅ All other requests require authentication
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}