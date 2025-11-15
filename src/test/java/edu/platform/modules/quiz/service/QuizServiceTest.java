package edu.platform.modules.quiz.service;

import edu.platform.modules.course.api.CourseAccessFacade;
import edu.platform.modules.course.entity.Course;
import edu.platform.modules.course.repository.CourseRepository;
import edu.platform.modules.quiz.dto.request.AnswerOptionRequest;
import edu.platform.modules.quiz.dto.request.QuestionRequest;
import edu.platform.modules.quiz.dto.request.QuizRequest;
import edu.platform.modules.quiz.dto.response.QuizResponse;
import edu.platform.modules.quiz.entity.Quiz;
import edu.platform.modules.quiz.enums.QuestionType;
import edu.platform.modules.quiz.repository.AnswerOptionRepository;
import edu.platform.modules.quiz.repository.QuestionRepository;
import edu.platform.modules.quiz.repository.QuizRepository;
import edu.platform.modules.user.entity.User;
import edu.platform.modules.user.enums.UserRole;
import edu.platform.modules.user.repository.UserRepository;
import edu.platform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
class QuizServiceTest extends AbstractIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CourseAccessFacade courseAccessFacade;

    @AfterEach
    void cleanData() {
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createQuizPersistsQuestionsAndOptions() {
        User teacher = persistTeacher("teacher-quiz@example.com");
        Course course = persistCourse("Course for quiz", teacher);
        Mockito.when(courseAccessFacade.getCourseEntityById(course.getId())).thenReturn(course);

        QuizResponse response = quizService.createQuiz(buildQuizRequest(course.getId()), teacher.getId());

        assertThat(response.getTitle()).isEqualTo("Integration Quiz");
        assertThat(quizRepository.findAll()).hasSize(1);
        assertThat(questionRepository.findAll()).hasSize(1);
        assertThat(answerOptionRepository.findAll()).hasSize(2);
    }

    @Test
    void createQuizFailsWhenTeacherDoesNotOwnCourse() {
        User teacher = persistTeacher("teacher-one@example.com");
        Course course = persistCourse("Another course", teacher);
        Mockito.when(courseAccessFacade.getCourseEntityById(course.getId())).thenReturn(course);

        assertThatThrownBy(() ->
                quizService.createQuiz(buildQuizRequest(course.getId()), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Teacher can only create quizzes");
    }

    private User persistTeacher(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .fullName("Teacher")
                .password("password")
                .role(UserRole.TEACHER)
                .build());
    }

    private Course persistCourse(String title, User teacher) {
        return courseRepository.save(Course.builder()
                .title(title)
                .description("Course description")
                .teacher(teacher)
                .build());
    }

    private QuizRequest buildQuizRequest(Long courseId) {
        AnswerOptionRequest firstOption = new AnswerOptionRequest();
        firstOption.setOptionText("Correct answer");
        firstOption.setIsCorrect(true);
        firstOption.setOrderIndex(1);

        AnswerOptionRequest secondOption = new AnswerOptionRequest();
        secondOption.setOptionText("Incorrect answer");
        secondOption.setIsCorrect(false);
        secondOption.setOrderIndex(2);

        QuestionRequest question = new QuestionRequest();
        question.setQuestionText("What is Spring?");
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setOrderIndex(1);
        question.setPoints(5);
        question.setAnswerOptions(List.of(firstOption, secondOption));

        QuizRequest request = new QuizRequest();
        request.setTitle("Integration Quiz");
        request.setDescription("Covers basics");
        request.setCourseId(courseId);
        request.setMaxAttempts(2);
        request.setTimeLimitMinutes(30);
        request.setQuestions(List.of(question));
        return request;
    }
}
