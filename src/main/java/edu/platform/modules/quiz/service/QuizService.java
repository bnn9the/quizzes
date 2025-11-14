package edu.platform.modules.quiz.service;

import edu.platform.modules.quiz.dto.request.QuizRequest;
import edu.platform.modules.quiz.dto.response.QuizResponse;
import edu.platform.modules.course.api.CourseAccessFacade;
import edu.platform.modules.course.entity.Course;
import edu.platform.modules.quiz.entity.AnswerOption;
import edu.platform.modules.quiz.entity.Question;
import edu.platform.modules.quiz.entity.Quiz;
import edu.platform.common.exception.ResourceNotFoundException;
import edu.platform.modules.quiz.mapper.QuizMapper;
import edu.platform.modules.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizMapper quizMapper;
    private final CourseAccessFacade courseAccessFacade;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public QuizResponse createQuiz(QuizRequest request, Long teacherId) {
        log.debug("Creating quiz with title: {} for course ID: {}", request.getTitle(), request.getCourseId());
        
        Course course = courseAccessFacade.getCourseEntityById(request.getCourseId());
        
        // Check if the teacher owns this course
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only create quizzes for their own courses");
        }
        
        Quiz quiz = quizMapper.toEntity(request);
        quiz.setCourse(course);
        quiz.setIsActive(true);
        
        Quiz savedQuiz = quizRepository.save(quiz);
        
        // Create questions and answer options
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = new ArrayList<>();
            
            for (var questionRequest : request.getQuestions()) {
                Question question = new Question();
                question.setQuiz(savedQuiz);
                question.setQuestionText(questionRequest.getQuestionText());
                question.setQuestionType(questionRequest.getQuestionType());
                question.setPoints(questionRequest.getPoints());
                question.setOrderIndex(questionRequest.getOrderIndex());
                
                Question savedQuestion = questionRepository.save(question);
                
                // Create answer options for choice questions
                if (questionRequest.getAnswerOptions() != null && !questionRequest.getAnswerOptions().isEmpty()) {
                    List<AnswerOption> answerOptions = new ArrayList<>();
                    
                    for (var optionRequest : questionRequest.getAnswerOptions()) {
                        AnswerOption answerOption = new AnswerOption();
                        answerOption.setQuestion(savedQuestion);
                        answerOption.setOptionText(optionRequest.getOptionText());
                        answerOption.setIsCorrect(optionRequest.getIsCorrect());
                        answerOption.setOrderIndex(optionRequest.getOrderIndex());
                        
                        answerOptions.add(answerOption);
                    }
                    
                    answerOptionRepository.saveAll(answerOptions);
                    savedQuestion.setAnswerOptions(answerOptions);
                }
                
                questions.add(savedQuestion);
            }
            
            savedQuiz.setQuestions(questions);
        }
        
        log.info("Quiz created successfully with ID: {}", savedQuiz.getId());
        return quizMapper.toResponse(savedQuiz);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        log.debug("Fetching quizzes for course ID: {}", courseId);
        
        List<Quiz> quizzes = quizRepository.findByCourseIdAndIsActiveTrue(courseId);
        return quizMapper.toResponseList(quizzes);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public QuizResponse getQuizById(Long id) {
        log.debug("Fetching quiz by ID: {}", id);
        
        Quiz quiz = quizRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        return quizMapper.toResponse(quiz);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<QuizResponse> getQuizzesByTeacher(Long teacherId) {
        log.debug("Fetching quizzes by teacher ID: {}", teacherId);
        
        List<Quiz> quizzes = quizRepository.findByTeacherId(teacherId);
        return quizMapper.toResponseList(quizzes);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public QuizResponse updateQuiz(Long id, QuizRequest request, Long teacherId) {
        log.debug("Updating quiz with ID: {} by teacher ID: {}", id, teacherId);
        
        Quiz quiz = quizRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        // Check if the teacher owns this quiz's course
        if (!quiz.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only update quizzes for their own courses");
        }
        
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        
        Quiz savedQuiz = quizRepository.save(quiz);
        log.info("Quiz updated successfully with ID: {}", savedQuiz.getId());
        
        return quizMapper.toResponse(savedQuiz);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void deleteQuiz(Long id, Long teacherId) {
        log.debug("Deleting quiz with ID: {} by teacher ID: {}", id, teacherId);
        
        Quiz quiz = quizRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        // Check if the teacher owns this quiz's course
        if (!quiz.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only delete quizzes for their own courses");
        }
        
        quiz.setIsActive(false);
        quizRepository.save(quiz);
        log.info("Quiz deactivated successfully with ID: {}", id);
    }
    
    // Internal method for other services
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Quiz getQuizEntityById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
    }
}



