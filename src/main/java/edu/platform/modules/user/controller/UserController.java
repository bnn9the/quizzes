package edu.platform.modules.user.controller;

import edu.platform.modules.user.dto.response.UserResponse;
import edu.platform.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Get information about the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.debug("Getting current user info for email: {}", email);
        
        UserResponse user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("Getting user by ID: {}", id);
        
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
}


