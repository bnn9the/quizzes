package edu.platform.service;

import edu.platform.dto.request.CourseVisitRequest;
import edu.platform.entity.*;
import edu.platform.entity.enums.VisitType;
import edu.platform.exception.ResourceNotFoundException;
import edu.platform.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
     * Manual creation of visit records (admin/back-office scenarios).
     */
    @Transactional
    public CourseVisit createVisit(CourseVisitRequest request) {
        Objects.requireNonNull(request, "Course visit request must not be null");
        log.debug("Creating manual visit record: {}", request);

        CourseVisit visit = buildVisit(
                request.getUserId(),
                request.getCourseId(),
                request.getLessonId(),
                request.getQuizId(),
                request.getVisitType(),
                null
        );

        if (request.getVisitedAt() != null) {
            visit.setVisitedAt(request.getVisitedAt());
        }
        visit.setDurationSeconds(request.getDurationSeconds());
        visit.setDeviceType(request.getDeviceType());
        visit.setIpAddress(request.getIpAddress());
        visit.setUserAgent(request.getUserAgent());

        return courseVisitRepository.save(visit);
    }

    /**
     * Fetch visits for a course in optional date range.
     */
    @Transactional(readOnly = true)
    public List<CourseVisit> getCourseVisits(Long courseId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return courseVisitRepository.findByCourseIdAndDateRange(courseId, startDate, endDate);
        }
        return courseVisitRepository.findByCourseIdOrderByVisitedAtDesc(courseId);
    }

    /**
     * Fetch visits for a user in optional date range.
     */
    @Transactional(readOnly = true)
    public List<CourseVisit> getUserVisits(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return courseVisitRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        }
        return courseVisitRepository.findByUserIdOrderByVisitedAtDesc(userId);
    }
    
    /**
     * Build a CourseVisit entity
     */
    private CourseVisit buildVisit(Long userId, Long courseId, Long lessonId, Long quizId,
                                   VisitType visitType, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        Lesson lesson = null;
        if (lessonId != null) {
            lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
            validateLessonOwnership(course, lesson);
        }

        Quiz quiz = null;
        if (quizId != null) {
            quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));
            validateQuizOwnership(course, quiz);
        }

        CourseVisit.CourseVisitBuilder builder = CourseVisit.builder()
                .user(user)
                .course(course)
                .lesson(lesson)
                .quiz(quiz)
                .visitType(visitType)
                .visitedAt(LocalDateTime.now());

        if (request != null) {
            builder.ipAddress(getClientIpAddress(request));
            builder.userAgent(request.getHeader("User-Agent"));
            builder.deviceType(detectDeviceType(request.getHeader("User-Agent")));
        }

        return builder.build();
    }

    private void validateLessonOwnership(Course course, Lesson lesson) {
        if (!lesson.getCourse().getId().equals(course.getId())) {
            throw new IllegalArgumentException(
                    String.format("Lesson %d does not belong to course %d", lesson.getId(), course.getId())
            );
        }
    }

    private void validateQuizOwnership(Course course, Quiz quiz) {
        if (!quiz.getCourse().getId().equals(course.getId())) {
            throw new IllegalArgumentException(
                    String.format("Quiz %d does not belong to course %d", quiz.getId(), course.getId())
            );
        }
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
