package klu.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import klu.util.JwtUtil;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    
    String path = request.getRequestURI();
    logger.debug("Processing request: {}", path);

    if (path.startsWith("/api/auth/") || path.startsWith("/api/password/")) {
        logger.debug("Public endpoint accessed, skipping JWT validation: {}", path);
        filterChain.doFilter(request, response);
        return;
    }

    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);
        try {
            username = jwtUtil.extractUsername(jwt);
            logger.debug("Extracted username from JWT: {}", username);
        } catch (Exception e) {
            logger.error("Invalid token: {}", e.getMessage());
        }
    } else {
        logger.debug("No JWT token found in request");
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        
        try {
            if (jwtUtil.validateToken(jwt, userDetails)) {
                logger.debug("JWT token is valid");
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                logger.debug("Set authentication in security context, authorities: {}", 
                        userDetails.getAuthorities());
            } else {
                logger.warn("JWT token validation failed");
            }
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
        }
    }

    filterChain.doFilter(request, response);
}
} 