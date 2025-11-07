package edu.platform.entity;

import edu.platform.entity.enums.VisitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a user visit to course content.
 * Tracks user activity for analytics and engagement metrics.
 */
@Entity
@Table(name = "course_visits", indexes = {
    @Index(name = "idx_course_visits_user", columnList = "user_id"),
    @Index(name = "idx_course_visits_course", columnList = "course_id"),
    @Index(name = "idx_course_visits_visited_at", columnList = "visited_at"),
    @Index(name = "idx_course_visits_user_course", columnList = "user_id,course_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseVisit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 20)
    @Builder.Default
    private VisitType visitType = VisitType.COURSE_VIEW;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "visited_at", nullable = false)
    @Builder.Default
    private LocalDateTime visitedAt = LocalDateTime.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}