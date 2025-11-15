package edu.platform.modules.user.service;

import edu.platform.modules.user.dto.request.LoginRequest;
import edu.platform.modules.user.dto.request.UserRegistrationRequest;
import edu.platform.modules.user.dto.response.AuthResponse;
import edu.platform.modules.user.dto.response.UserResponse;
import edu.platform.modules.user.entity.User;
import edu.platform.common.exception.InvalidCredentialsException;
import edu.platform.modules.user.mapper.UserMapper;
import edu.platform.modules.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public AuthResponse register(UserRegistrationRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());
        
        UserResponse user = userService.registerUser(request);
        String token = jwtUtil.generateToken(user.getEmail());
        
        log.info("User registered and authenticated successfully: {}", user.getEmail());
        return new AuthResponse(token, user);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AuthResponse login(LoginRequest request) {
        log.debug("Authenticating user with email: {}", request.getEmail());
        
        User user = userService.findUserEntityByEmail(request.getEmail());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);
        
        log.info("User authenticated successfully: {}", user.getEmail());
        return new AuthResponse(token, userResponse);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean validateToken(String token, String email) {
        try {
            return jwtUtil.validateToken(token, email);
        } catch (Exception e) {
            log.warn("Token validation failed for email: {}", email, e);
            return false;
        }
    }
    
    public String extractEmailFromToken(String token) {
        try {
            return jwtUtil.extractEmail(token);
        } catch (Exception e) {
            log.warn("Failed to extract email from token", e);
            return null;
        }
    }
}


