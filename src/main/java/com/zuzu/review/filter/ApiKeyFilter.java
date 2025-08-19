package com.zuzu.review.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.security.apiKey}")
    private String expectedApiKey;
    
    // whitelist paths that should be public
    private static final List<String> WHITELIST = List.of(
        "/health",              // your HealthController
        "/ping",                // optional
        "/actuator/health"      // if you use actuator
    );
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
      String path = request.getRequestURI();
      // exact matches
      if (WHITELIST.contains(path)) return true;
      // simple prefix matches (add more if you like)
      return path.startsWith("/actuator/") || path.startsWith("/swagger-ui/") ;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        if (expectedApiKey.equals(apiKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Invalid API Key");
        }
    }
}

