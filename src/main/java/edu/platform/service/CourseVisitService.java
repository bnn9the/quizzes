package edu.platform.service;

import edu.platform.entity.*;
import edu.platform.entity.enums.VisitType;
import edu.platform.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for tracking user visits to course content.
 * Provides asynchronous visit recording to avoid blocking main operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseVisitService {
    
    private final CourseVisitRepository courseVisitRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    
    /**
     * Record a course view asynchronously
     */
    @Async
    @Transactional
    public void recordCourseView(Long userId, Long courseId, HttpServletRequest request) {
        try {
            log.debug("Recording course view: user={}, course={}", userId, courseId);
            
            CourseVisit visit = buildVisit(userId, courseId, null, null, 
                                          VisitType.COURSE_VIEW, request);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record course view: user={}, course={}", userId, courseId, e);
            // Don't throw - visit tracking should not break main flow
        }
    }
    
    /**
     * Record a lesson view asynchronously
     */
    @Async
    @Transactional
    public void recordLessonView(Long userId, Long courseId, Long lessonId, 
                                  HttpServletRequest request) {
        try {
            log.debug("Recording lesson view: user={}, course={}, lesson={}", 
                     userId, courseId, lessonId);
            
            CourseVisit visit = buildVisit(userId, courseId, lessonId, null, 
                                          VisitType.LESSON_VIEW, request);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record lesson view: user={}, lesson={}", userId, lessonId, e);
        }
    }
    
    /**
     * Record a quiz view asynchronously
     */
    @Async
    @Transactional
    public void recordQuizView(Long userId, Long courseId, Long quizId, 
                                HttpServletRequest request) {
        try {
            log.debug("Recording quiz view: user={}, course={}, quiz={}", 
                     userId, courseId, quizId);
            
            CourseVisit visit = buildVisit(userId, courseId, null, quizId, 
                                          VisitType.QUIZ_VIEW, request);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record quiz view: user={}, quiz={}", userId, quizId, e);
        }
    }
    
    /**
     * Record quiz start
     */
    @Async
    @Transactional
    public void recordQuizStart(Long userId, Long courseId, Long quizId, 
                                 HttpServletRequest request) {
        try {
            log.debug("Recording quiz start: user={}, quiz={}", userId, quizId);
            
            CourseVisit visit = buildVisit(userId, courseId, null, quizId, 
                                          VisitType.QUIZ_START, request);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record quiz start: user={}, quiz={}", userId, quizId, e);
        }
    }
    
    /**
     * Record quiz completion
     */
    @Async
    @Transactional
    public void recordQuizCompletion(Long userId, Long courseId, Long quizId, 
                                     Integer durationSeconds, HttpServletRequest request) {
        try {
            log.debug("Recording quiz completion: user={}, quiz={}, duration={}", 
                     userId, quizId, durationSeconds);
            
            CourseVisit visit = buildVisit(userId, courseId, null, quizId, 
                                          VisitType.QUIZ_COMPLETE, request);
            visit.setDurationSeconds(durationSeconds);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record quiz completion: user={}, quiz={}", userId, quizId, e);
        }
    }
    
    /**
     * Record course enrollment
     */
    @Async
    @Transactional
    public void recordCourseEnrollment(Long userId, Long courseId, HttpServletRequest request) {
        try {
            log.debug("Recording course enrollment: user={}, course={}", userId, courseId);
            
            CourseVisit visit = buildVisit(userId, courseId, null, null, 
                                          VisitType.COURSE_ENROLLMENT, request);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record enrollment: user={}, course={}", userId, courseId, e);
        }
    }
    
    /**
     * Record lesson completion
     */
    @Async
    @Transactional
    public void recordLessonCompletion(Long userId, Long courseId, Long lessonId, 
                                       Integer durationSeconds, HttpServletRequest request) {
        try {
            log.debug("Recording lesson completion: user={}, lesson={}, duration={}", 
                     userId, lessonId, durationSeconds);
            
            CourseVisit visit = buildVisit(userId, courseId, lessonId, null, 
                                          VisitType.LESSON_COMPLETE, request);
            visit.setDurationSeconds(durationSeconds);
            courseVisitRepository.save(visit);
        } catch (Exception e) {
            log.error("Failed to record lesson completion: user={}, lesson={}", 
                     userId, lessonId, e);
        }
    }
    
    /**
     * Build a CourseVisit entity
     */
    private CourseVisit buildVisit(Long userId, Long courseId, Long lessonId, Long quizId,
                                   VisitType visitType, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        
        CourseVisit.CourseVisitBuilder builder = CourseVisit.builder()
                .user(user)
                .course(course)
                .visitType(visitType)
                .visitedAt(LocalDateTime.now());
        
        // Add lesson if provided
        if (lessonId != null) {
            lessonRepository.findById(lessonId)
                    .ifPresent(builder::lesson);
        }
        
        // Add quiz if provided
        if (quizId != null) {
            quizRepository.findById(quizId)
                    .ifPresent(builder::quiz);
        }
        
        // Extract request metadata
        if (request != null) {
            builder.ipAddress(getClientIpAddress(request));
            builder.userAgent(request.getHeader("User-Agent"));
            builder.deviceType(detectDeviceType(request.getHeader("User-Agent")));
        }
        
        return builder.build();
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Detect device type from user agent
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return "UNKNOWN";
        }
        
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        } else {
            return "DESKTOP";
        }
    }
}