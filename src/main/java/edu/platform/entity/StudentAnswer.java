package edu.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "student_answers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    
    @ElementCollection
    @CollectionTable(name = "student_answer_selected_options", joinColumns = @JoinColumn(name = "student_answer_id"))
    @Column(name = "selected_option_id")
    private List<Long> selectedOptionIds;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column(name = "points_earned", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pointsEarned = BigDecimal.ZERO;
}
