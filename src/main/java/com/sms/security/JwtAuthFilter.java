package com.sms.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }

    /**
     * This filter runs ONCE per HTTP request — before it reaches any controller.
     *
     * What it does, step by step:
     *   1. Look for the Authorization header  →  "Bearer eyJhbGci..."
     *   2. Extract the token (everything after "Bearer ")
     *   3. Extract the username from the token
     *   4. Load the user from the database
     *   5. Validate the token (correct user? not expired?)
     *   6. Tell Spring Security "this user is authenticated"
     *   7. Pass the request on to the controller
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1 — grab the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If there's no Authorization header, or it doesn't start with "Bearer ",
        // skip JWT processing and move on (the request might be a public endpoint)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // Step 4 — only process if we have a username and no auth is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load full user details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Step 5 — validate: does the token match this user and is it still valid?
            if (jwtUtil.isTokenValid(jwt, userDetails)) {

                // Step 6 — create an authentication object and give it to Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // no credentials needed (token-based)
                                userDetails.getAuthorities()   // ROLE_ADMIN / ROLE_TEACHER / ROLE_STUDENT
                        );

                // Attach request details (IP address, session, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Tell Spring Security this request is now authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 7 — pass the request along to the next filter / controller
        filterChain.doFilter(request, response);
    }
}
