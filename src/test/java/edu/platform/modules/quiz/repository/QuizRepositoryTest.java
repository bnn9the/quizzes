package edu.platform.modules.quiz.repository;

import edu.platform.modules.course.entity.Course;
import edu.platform.modules.quiz.entity.AnswerOption;
import edu.platform.modules.quiz.entity.Question;
import edu.platform.modules.quiz.entity.Quiz;
import edu.platform.modules.quiz.enums.QuestionType;
import edu.platform.modules.user.entity.User;
import edu.platform.modules.user.enums.UserRole;
import edu.platform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuizRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByCourseIdAndIsActiveTrueReturnsOnlyActiveQuizzes() {
        User teacher = persistTeacher("active-teacher@example.com");
        Course course = persistCourse("Course A", teacher);
        persistQuiz("Inactive quiz", course, false);
        Quiz activeQuiz = persistQuiz("Active quiz", course, true);

        List<Quiz> quizzes = quizRepository.findByCourseIdAndIsActiveTrue(course.getId());

        assertThat(quizzes)
                .hasSize(1)
                .first()
                .extracting(Quiz::getTitle)
                .isEqualTo(activeQuiz.getTitle());
    }

    @Test
    void findByIdWithFullDetailsLoadsQuestionsAndAnswers() {
        User teacher = persistTeacher("full-details@example.com");
        Course course = persistCourse("Course B", teacher);
        Quiz quiz = persistQuiz("Java Basics", course, true);
        Question question = persistQuestion(quiz, "What is JVM?", 1);
        persistAnswerOption(question, "Virtual Machine", true, 1);

        Quiz result = quizRepository.findByIdWithFullDetails(quiz.getId())
                .orElseThrow();

        assertThat(result.getQuestions())
                .hasSize(1)
                .first()
                .satisfies(q -> assertThat(q.getAnswerOptions()).hasSize(1));
    }

    private User persistTeacher(String email) {
        User teacher = User.builder()
                .email(email)
                .fullName("Teacher")
                .password("password")
                .role(UserRole.TEACHER)
                .build();
        return entityManager.persist(teacher);
    }

    private Course persistCourse(String title, User teacher) {
        Course course = Course.builder()
                .title(title)
                .description("Description")
                .teacher(teacher)
                .build();
        return entityManager.persist(course);
    }

    private Quiz persistQuiz(String title, Course course, boolean active) {
        Quiz quiz = Quiz.builder()
                .title(title)
                .description("Description")
                .course(course)
                .maxAttempts(1)
                .timeLimitMinutes(30)
                .isActive(active)
                .build();
        return entityManager.persist(quiz);
    }

    private Question persistQuestion(Quiz quiz, String text, int orderIndex) {
        Question question = Question.builder()
                .quiz(quiz)
                .questionText(text)
                .questionType(QuestionType.SINGLE_CHOICE)
                .points(5)
                .orderIndex(orderIndex)
                .build();
        return entityManager.persist(question);
    }

    private void persistAnswerOption(Question question, String text, boolean correct, int orderIndex) {
        AnswerOption option = AnswerOption.builder()
                .question(question)
                .optionText(text)
                .isCorrect(correct)
                .orderIndex(orderIndex)
                .build();
        entityManager.persist(option);
    }
}
