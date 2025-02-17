package com.example.news.configurations;

import com.example.news.filters.JwtFilter;
import com.example.news.handler.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    @Autowired
    public SecurityConfig(JwtFilter jwtFilter){
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // Ensure this is added
                .addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/v1/media_file/uploads/**").permitAll()
                        .requestMatchers("/api/v1/tag/filter").permitAll()
                        .requestMatchers("/api/v1/category/filter").permitAll()
                        .requestMatchers("/api/v1/article/slug/**").permitAll()
                        .requestMatchers("/api/v1/article/category/**").permitAll()
                        .requestMatchers("/api/v1/article/increment/**").permitAll()
                        .requestMatchers("/api/v1/user/login").permitAll()
                        .requestMatchers("/api/v1/user/register").hasRole("ADMIN")
                        .requestMatchers("/api/v1/user/delete/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/user/update/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/user/filter/**").hasRole("ADMIN")
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
//                .oauth2Login(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults());
//                .sessionManagement(sessionManagement ->
//                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Không tạo session
//                );
//        http.oauth2Login(oauth2 ->
//                oauth2
//                        .successHandler(authenticationSuccessHandler())
//        );
//                .addFilterBefore(new JwtTokenFilter(), UsernamePasswordAuthenticationFilter.class); // Thêm filter tùy chỉnh của bạn
        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Origin", "Authorization", "content-type", "x-auth-token"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
