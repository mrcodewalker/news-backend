package com.example.news.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
                response.getWriter().write(createJsonResponse(401, "Unauthorized please try again!"));
                return;
            }

            final String jwt = authHeader.substring(7);
            final String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(createJsonResponse(401, "Invalid token: " + e.getMessage()));
        }
    }

    private boolean isByPassToken(@NonNull HttpServletRequest request) {
        String apiPrefix = "/api/v1";
        final List<String> byPassPaths = List.of(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources",
                "/swagger-resources/**",
                "/webjars",
                "/webjars/**",
                "/v2/api-docs",
                "/v2/api-docs/**",
                "/swagger-ui/index.html",
                "/swagger-ui/index.html/**",
                String.format("%s/user/login", apiPrefix),
                String.format("%s/article/slug", apiPrefix),
                String.format("%s/article/category", apiPrefix),
                String.format("%s/article/by_tags", apiPrefix),
                String.format("%s/article/by_sub", apiPrefix),
                String.format("%s/article/home", apiPrefix),
                String.format("%s/article/star", apiPrefix),
                String.format("%s/article/hot", apiPrefix),
                String.format("%s/article/paging", apiPrefix),
                String.format("%s/category/filter", apiPrefix),
                String.format("%s/tag/filter", apiPrefix),
                String.format("%s/sub_category/filter", apiPrefix),
                String.format("%s/article/increment", apiPrefix),
                String.format("%s/media_file/uploads", apiPrefix)
        );

        String requestPath = request.getServletPath();
        return byPassPaths.stream().anyMatch(path -> requestPath.startsWith(path));
    }

    private static String createJsonResponse(int status, String message) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", status);
        responseMap.put("message", message);
        responseMap.put("data", null);

        try {
            return objectMapper.writeValueAsString(responseMap);
        } catch (Exception e) {
            return "{\"status\":" + status + ",\"message\":\"" + message + "\",\"data\":null}";
        }
    }
}

