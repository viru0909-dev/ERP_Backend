package com.sih.erp.controller;

import com.sih.erp.dto.JwtAuthenticationResponse;
import com.sih.erp.dto.LoginRequest;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.dto.UserRegistrationRequest;
import com.sih.erp.entity.User;
import com.sih.erp.service.UserDetailsServiceImpl;
import com.sih.erp.service.UserService;
import com.sih.erp.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        User registeredUser = userService.registerUser(registrationRequest);

        // --- THIS IS THE FIX ---
        // Convert the new user to a DTO before sending it back to the frontend.
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setUserId(registeredUser.getUserId());
        userProfileDto.setFullName(registeredUser.getFullName());
        userProfileDto.setEmail(registeredUser.getEmail());
        userProfileDto.setContactNumber(registeredUser.getContactNumber());
        userProfileDto.setRole(registeredUser.getRole());

        return new ResponseEntity<>(userProfileDto, HttpStatus.CREATED);
    }

    // --- THIS IS THE NEW LOGIN ENDPOINT ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 1. Attempt to authenticate the user using the provided email and password.
            // Spring Security handles the password checking against the hashed version in the database.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // 2. If authentication fails, throw a clear error.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        // 3. If authentication is successful, load the user's details.
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        // 4. Generate a JWT for this user.
        final String token = jwtUtil.generateToken(userDetails);

        // 5. Return the token in the response body.
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }
}