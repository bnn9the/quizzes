package edu.platform.common.analytics;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Contract for recording high-level user activity events that other modules rely on.
 */
public interface VisitTrackingFacade {

    void recordCourseView(Long userId, Long courseId, HttpServletRequest request);

    void recordLessonView(Long userId, Long courseId, Long lessonId, HttpServletRequest request);

    void recordQuizView(Long userId, Long courseId, Long quizId, HttpServletRequest request);

    void recordQuizStart(Long userId, Long courseId, Long quizId, HttpServletRequest request);

    void recordQuizCompletion(Long userId, Long courseId, Long quizId, Integer durationSeconds,
                              HttpServletRequest request);
}
