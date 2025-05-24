package com.example.news.configurations;

import com.example.news.filters.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("{\"status\":401,\"message\":\"Unauthorized: " + authException.getMessage() + "\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("{\"status\":403,\"message\":\"Access denied: " + accessDeniedException.getMessage() + "\",\"data\":null}");
                })
            )
            .addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", 
                               "/swagger-resources/**", "/webjars/**", "/v2/api-docs/**",
                               "/swagger-ui/index.html", "/swagger-ui/index.html/**").permitAll()
                .requestMatchers("/api/v1/media_file/uploads/**").permitAll()
                .requestMatchers("/api/v1/tag/filter").permitAll()
                .requestMatchers("/api/v1/category/filter").permitAll()
                .requestMatchers("/api/v1/article/slug/**").permitAll()
                .requestMatchers("/api/v1/article/category/**").permitAll()
                .requestMatchers("/api/v1/article/by_tags").permitAll()
                .requestMatchers("/api/v1/article/star").permitAll()
                .requestMatchers("/api/v1/article/paging/**").permitAll()
                .requestMatchers("/api/v1/article/by_sub/**").permitAll()
                .requestMatchers("/api/v1/article/home").permitAll()
                .requestMatchers("/api/v1/article/hot/**").permitAll()
                .requestMatchers("/api/v1/article/increment/**").permitAll()
                .requestMatchers("/api/v1/user/login").permitAll()
                .requestMatchers("/api/v1/sub_category/filter").permitAll()
                .requestMatchers("/api/v1/sub_category/create").hasRole("ADMIN")
                .requestMatchers("/api/v1/sub_category/update").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/register").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/delete/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/update/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/article/draft/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/view/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/create").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/update/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/delete/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/filter/page/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/media_file/views/pdf").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/status/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/search/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/match/tag/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/article/filter/tags").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/category/create").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/category/update/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/category/delete/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/tag/create").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/tag/update/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/tag/delete/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/media_file/filter").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/media_file/upload").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/article/**/increment-view").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-API-KEY"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
