package edu.platform.modules.user.service;

import edu.platform.modules.user.dto.request.UserRegistrationRequest;
import edu.platform.modules.user.dto.response.UserResponse;
import edu.platform.modules.user.entity.User;
import edu.platform.modules.user.enums.UserRole;
import edu.platform.common.exception.ResourceNotFoundException;
import edu.platform.common.exception.UserAlreadyExistsException;
import edu.platform.modules.user.mapper.UserMapper;
import edu.platform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.debug("Registering user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserResponse findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserResponse findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserResponse> findByRole(UserRole role) {
        log.debug("Finding users by role: {}", role);
        
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public UserResponse findByIdWithLock(Long id) {
        log.debug("Finding user by ID with lock: {}", id);
        
        User user = userRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Internal method for authentication service
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    // Helper method to get User entity from current authentication
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public User getCurrentUserEntity(String email) {
        return findUserEntityByEmail(email);
    }
}




