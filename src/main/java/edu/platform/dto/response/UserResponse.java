package edu.platform.dto.response;

import edu.platform.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "User response")
public class UserResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "User email", example = "user@example.com")
    private String email;
    
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "User role", example = "STUDENT")
    private UserRole role;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
