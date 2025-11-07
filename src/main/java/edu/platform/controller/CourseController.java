package edu.platform.controller;

import edu.platform.dto.request.CourseRequest;
import edu.platform.dto.response.CourseResponse;
import edu.platform.entity.User;
import edu.platform.service.CourseService;
import edu.platform.service.CourseVisitService;
import edu.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Management", description = "Course management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {
    
    private final CourseService courseService;
    private final UserService userService;
    private final CourseVisitService courseVisitService;
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new course", description = "Create a new course (TEACHER or ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course created successfully",
                content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Course creation request received from: {}", email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        CourseResponse course = courseService.createCourse(request, currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }
    
    @GetMapping
    @Operation(summary = "Get all courses", description = "Get all available courses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        log.debug("Getting all courses");
        
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Get course details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course found",
                content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseResponse> getCourseById(
            @Parameter(description = "Course ID") @PathVariable Long id,
            HttpServletRequest request) {
        log.debug("Getting course by ID: {}", id);
        
        CourseResponse course = courseService.getCourseById(id);
        recordCourseView(id, request);
        return ResponseEntity.ok(course);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Update course", description = "Update course (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course updated successfully",
                content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseResponse> updateCourse(
            @Parameter(description = "Course ID") @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Course update request received for ID: {} from: {}", id, email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        CourseResponse course = courseService.updateCourse(id, request, currentUser.getId());
        
        return ResponseEntity.ok(course);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Delete course", description = "Delete course (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "Course ID") @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Course deletion request received for ID: {} from: {}", id, email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        courseService.deleteCourse(id, currentUser.getId());
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get my courses", description = "Get courses created by current teacher")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<List<CourseResponse>> getMyCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.debug("Getting courses for teacher: {}", email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        List<CourseResponse> courses = courseService.getCoursesByTeacher(currentUser.getId());
        
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search courses by title")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<List<CourseResponse>> searchCourses(
            @Parameter(description = "Search query") @RequestParam String q) {
        log.debug("Searching courses with query: {}", q);
        
        List<CourseResponse> courses = courseService.searchCoursesByTitle(q);
        return ResponseEntity.ok(courses);
    }

    private void recordCourseView(Long courseId, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return;
        }
        try {
            Long userId = userService.getCurrentUserEntity(authentication.getName()).getId();
            courseVisitService.recordCourseView(userId, courseId, request);
        } catch (Exception ex) {
            log.debug("Failed to record course visit for course {}", courseId, ex);
        }
    }
}
