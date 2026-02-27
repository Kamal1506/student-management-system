package com.sms.service;

import com.sms.dto.LoginRequest;
import com.sms.dto.LoginResponse;
import com.sms.dto.RegisterRequest;
import com.sms.model.User;
import com.sms.repository.UserRepository;
import com.sms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService   userDetailsService;

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    /**
     * What happens when a user clicks "Login":
     *
     *  1. Frontend sends  { username, password }  to POST /api/auth/login
     *  2. AuthenticationManager checks: does this user exist? is the password correct?
     *     → If wrong: throws BadCredentialsException → 401 Unauthorized
     *     → If correct: continues
     *  3. We load the full UserDetails from the DB
     *  4. JwtUtil generates a signed token
     *  5. We return { token, role, username } to the frontend
     *  6. Frontend stores the token in localStorage and redirects to the right dashboard
     */
    public LoginResponse login(LoginRequest request) {

        // Step 2 — this line does all the credential checking
        // If username or password is wrong, it throws an exception automatically
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Step 3 — load user details (needed to generate the token)
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Step 4 — generate the JWT token
        String token = jwtUtil.generateToken(userDetails);

        // Step 5 — fetch the role from DB to include in response
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        return new LoginResponse(
                token,
                user.getRole().name(),     // "ADMIN" | "TEACHER" | "STUDENT"
                user.getUsername()
        );
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────
    /**
     * Creates a new user account.
     * Called by Admin when adding a new student or teacher.
     *
     * IMPORTANT: the password is BCrypt-hashed before saving.
     * We never store plain-text passwords in the database.
     */
    public String register(RegisterRequest request) {

        // Check if username is already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // hash it!
                .role(request.getRole())
                .enabled(true)
                .build();

        userRepository.save(user);

        return "User registered successfully: " + user.getUsername();
    }
}