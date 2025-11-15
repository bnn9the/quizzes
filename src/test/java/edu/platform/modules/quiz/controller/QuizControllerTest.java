package edu.platform.modules.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.platform.common.analytics.VisitTrackingFacade;
import edu.platform.modules.course.api.CourseAccessFacade;
import edu.platform.modules.course.entity.Course;
import edu.platform.modules.course.repository.CourseRepository;
import edu.platform.modules.quiz.dto.request.AnswerOptionRequest;
import edu.platform.modules.quiz.dto.request.QuestionRequest;
import edu.platform.modules.quiz.dto.request.QuizRequest;
import edu.platform.modules.quiz.entity.AnswerOption;
import edu.platform.modules.quiz.entity.Question;
import edu.platform.modules.quiz.entity.Quiz;
import edu.platform.modules.quiz.enums.QuestionType;
import edu.platform.modules.quiz.repository.AnswerOptionRepository;
import edu.platform.modules.quiz.repository.QuestionRepository;
import edu.platform.modules.quiz.repository.QuizRepository;
import edu.platform.modules.user.api.UserAccessFacade;
import edu.platform.modules.user.entity.User;
import edu.platform.modules.user.enums.UserRole;
import edu.platform.modules.user.repository.UserRepository;
import edu.platform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private UserAccessFacade userAccessFacade;

    @MockBean
    private CourseAccessFacade courseAccessFacade;

    @MockBean
    private VisitTrackingFacade visitTrackingFacade;

    @AfterEach
    void cleanUp() {
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = {"TEACHER"})
    void createQuizAsTeacherReturnsCreated() throws Exception {
        User teacher = persistUser("teacher@example.com", UserRole.TEACHER);
        Course course = persistCourse("Controller course", teacher);

        Mockito.when(userAccessFacade.getUserByEmail("teacher@example.com")).thenReturn(teacher);
        Mockito.when(courseAccessFacade.getCourseEntityById(course.getId())).thenReturn(course);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildQuizRequest(course.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Controller Quiz"));

        assertThat(quizRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void createQuizAsStudentIsForbidden() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildQuizRequest(1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getQuizByIdReturnsQuizAndRecordsVisit() throws Exception {
        User teacher = persistUser("creator@example.com", UserRole.TEACHER);
        Course course = persistCourse("Course with quiz", teacher);
        Quiz quiz = persistQuizWithData(course);
        User student = persistUser("student@example.com", UserRole.STUDENT);

        Mockito.when(userAccessFacade.getUserByEmail("student@example.com")).thenReturn(student);

        mockMvc.perform(get("/api/quizzes/{id}", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quiz.getId()));

        verify(visitTrackingFacade).recordQuizView(eq(student.getId()), eq(course.getId()), eq(quiz.getId()), any());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void getQuizByIdReturnsNotFoundForMissingQuiz() throws Exception {
        mockMvc.perform(get("/api/quizzes/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    private QuizRequest buildQuizRequest(Long courseId) {
        AnswerOptionRequest option = new AnswerOptionRequest();
        option.setOptionText("Correct");
        option.setIsCorrect(true);
        option.setOrderIndex(1);

        QuestionRequest question = new QuestionRequest();
        question.setQuestionText("What is Spring Boot?");
        question.setQuestionType(QuestionType.SINGLE_CHOICE);
        question.setOrderIndex(1);
        question.setPoints(5);
        question.setAnswerOptions(List.of(option));

        QuizRequest request = new QuizRequest();
        request.setTitle("Controller Quiz");
        request.setDescription("Created via controller");
        request.setCourseId(courseId);
        request.setMaxAttempts(1);
        request.setTimeLimitMinutes(15);
        request.setQuestions(List.of(question));
        return request;
    }

    private User persistUser(String email, UserRole role) {
        return userRepository.save(User.builder()
                .email(email)
                .password("password")
                .fullName("User " + email)
                .role(role)
                .build());
    }

    private Course persistCourse(String title, User teacher) {
        return courseRepository.save(Course.builder()
                .title(title)
                .description("Course description")
                .teacher(teacher)
                .build());
    }

    private Quiz persistQuizWithData(Course course) {
        Quiz quiz = quizRepository.save(Quiz.builder()
                .title("Persisted quiz")
                .description("Quiz description")
                .course(course)
                .maxAttempts(1)
                .isActive(true)
                .build());

        Question question = questionRepository.save(Question.builder()
                .quiz(quiz)
                .questionText("How many JDKs exist?")
                .orderIndex(1)
                .points(2)
                .questionType(QuestionType.SINGLE_CHOICE)
                .build());

        answerOptionRepository.save(AnswerOption.builder()
                .question(question)
                .optionText("Many")
                .isCorrect(true)
                .orderIndex(1)
                .build());

        return quiz;
    }
}
