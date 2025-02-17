package com.example.news.filters;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtUtil jwtTokenProvider;
    private UserDetailsService userDetailsService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    public JwtFilter(JwtUtil jwtTokenProvider,
                     UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    public JwtFilter() {
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (this.isByPassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(createJsonResponse(401, "Hệ thống đã ghi lại IP đáng ngờ của bạn"));
                return;
            }

            final String token = authHeader.substring(7);

            final String username = jwtTokenProvider.extractUserName(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtTokenProvider.validateToken(token, username)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(createJsonResponse(401, "Lỗi hệ thống, hãy thử lại"));
        }
    }
    private boolean isByPassToken(@NonNull HttpServletRequest request) {
        String apiPrefix = "/api/v1";
        final List<Pair<String,String>> byPassTokens = List.of(
//                Pair.of(String.format("%s/user/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/user/login", apiPrefix), "POST"),

                Pair.of(String.format("%s/user/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/article/slug", apiPrefix), "GET"),
                Pair.of(String.format("%s/article/category", apiPrefix), "GET"),
                Pair.of(String.format("%s/category/filter", apiPrefix), "GET"),
                Pair.of(String.format("%s/tag/filter", apiPrefix), "GET"),
                Pair.of(String.format("%s/article/increment", apiPrefix), "POST"),
                Pair.of(String.format("%s/media_file/uploads", apiPrefix), "GET"),
                Pair.of(String.format("%s/user/login", apiPrefix), "POST")
        );
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        for (Pair<String, String> bypassToken : byPassTokens) {
            if (requestPath.contains(bypassToken.getFirst())
                    && requestMethod.equals(bypassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }
    private String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    public static String createJsonResponse(int status, String message) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", status);
        responseMap.put("message", message);

        try {
            return objectMapper.writeValueAsString(responseMap);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\": 500, \"message\": \"Error creating JSON response\"}";
        }
    }

}

