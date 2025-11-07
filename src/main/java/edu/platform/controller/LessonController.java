package edu.platform.controller;

import edu.platform.dto.request.LessonRequest;
import edu.platform.dto.response.LessonResponse;
import edu.platform.service.CourseVisitService;
import edu.platform.service.LessonService;
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
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson Management", description = "Lesson management APIs")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {
    
    private final LessonService lessonService;
    private final UserService userService;
    private final CourseVisitService courseVisitService;
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new lesson", description = "Create a new lesson for a course (TEACHER or ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Lesson created successfully",
                content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody LessonRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Lesson creation request from: {} for course: {}", email, request.getCourseId());
        
        LessonResponse lesson = lessonService.createLesson(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", description = "Get lesson details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson found",
                content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<LessonResponse> getLessonById(
            @Parameter(description = "Lesson ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.debug("Getting lesson by ID: {}", id);
        
        LessonResponse lesson = lessonService.getLessonById(id);
        recordLessonView(lesson, request);
        return ResponseEntity.ok(lesson);
    }
    
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get lessons by course", description = "Get all lessons for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    })
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        
        log.debug("Getting lessons for course: {}", courseId);
        
        List<LessonResponse> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(lessons);
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get my lessons", description = "Get lessons created by current teacher")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    })
    public ResponseEntity<List<LessonResponse>> getMyLessons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.debug("Getting lessons for teacher: {}", email);
        
        List<LessonResponse> lessons = lessonService.getLessonsByTeacher(teacherId);
        return ResponseEntity.ok(lessons);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Update lesson", description = "Update lesson (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson updated successfully",
                content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Lesson not found"),
        @ApiResponse(responseCode = "409", description = "Optimistic locking failure - lesson was modified by another user")
    })
    public ResponseEntity<LessonResponse> updateLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long id,
            @Valid @RequestBody LessonRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Lesson update request for ID: {} from: {}", id, email);
        
        LessonResponse lesson = lessonService.updateLesson(id, request, teacherId);
        return ResponseEntity.ok(lesson);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Delete lesson", description = "Delete lesson (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Lesson deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long id) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Lesson deletion request for ID: {} from: {}", id, email);
        
        lessonService.deleteLesson(id, teacherId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{lessonId}/media/{mediaAssetId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Attach media to lesson", description = "Attach a media asset to a lesson")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media attached successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Lesson or media asset not found")
    })
    public ResponseEntity<LessonResponse> attachMedia(
            @Parameter(description = "Lesson ID") @PathVariable Long lessonId,
            @Parameter(description = "Media asset ID") @PathVariable Long mediaAssetId) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Attaching media {} to lesson {} by {}", mediaAssetId, lessonId, email);
        
        LessonResponse lesson = lessonService.attachMediaToLesson(lessonId, mediaAssetId, teacherId);
        return ResponseEntity.ok(lesson);
    }
    
    @DeleteMapping("/{lessonId}/media/{mediaAssetId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Detach media from lesson", description = "Remove a media asset from a lesson")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media detached successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Lesson or media asset not found")
    })
    public ResponseEntity<LessonResponse> detachMedia(
            @Parameter(description = "Lesson ID") @PathVariable Long lessonId,
            @Parameter(description = "Media asset ID") @PathVariable Long mediaAssetId) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long teacherId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Detaching media {} from lesson {} by {}", mediaAssetId, lessonId, email);
        
        LessonResponse lesson = lessonService.detachMediaFromLesson(lessonId, mediaAssetId, teacherId);
        return ResponseEntity.ok(lesson);
    }

    private void recordLessonView(LessonResponse lesson, HttpServletRequest request) {
        if (lesson == null) {
            return;
        }
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
            courseVisitService.recordLessonView(userId, lesson.getCourseId(), lesson.getId(), request);
        } catch (Exception ex) {
            log.debug("Failed to record lesson visit for lesson {}", lesson.getId(), ex);
        }
    }
}
