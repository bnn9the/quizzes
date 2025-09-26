package edu.platform.service;

import edu.platform.dto.request.CourseRequest;
import edu.platform.dto.response.CourseResponse;
import edu.platform.entity.Course;
import edu.platform.entity.User;
import edu.platform.exception.ResourceNotFoundException;
import edu.platform.mapper.CourseMapper;
import edu.platform.repository.CourseRepository;
import edu.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CourseResponse createCourse(CourseRequest request, Long teacherId) {
        log.debug("Creating course with title: {} for teacher ID: {}", request.getTitle(), teacherId);
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));
        
        Course course = courseMapper.toEntity(request);
        course.setTeacher(teacher);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());
        
        return courseMapper.toResponse(savedCourse);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<CourseResponse> getAllCourses() {
        log.debug("Fetching all courses");
        
        List<Course> courses = courseRepository.findAllWithTeacher();
        return courseMapper.toResponseList(courses);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public CourseResponse getCourseById(Long id) {
        log.debug("Fetching course by ID: {}", id);
        
        Course course = courseRepository.findByIdWithTeacher(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        
        return courseMapper.toResponse(course);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<CourseResponse> getCoursesByTeacher(Long teacherId) {
        log.debug("Fetching courses by teacher ID: {}", teacherId);
        
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        return courseMapper.toResponseList(courses);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public CourseResponse updateCourse(Long id, CourseRequest request, Long teacherId) {
        log.debug("Updating course with ID: {} by teacher ID: {}", id, teacherId);
        
        Course course = courseRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        
        // Check if the teacher owns this course
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only update their own courses");
        }
        
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course updated successfully with ID: {}", savedCourse.getId());
        
        return courseMapper.toResponse(savedCourse);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void deleteCourse(Long id, Long teacherId) {
        log.debug("Deleting course with ID: {} by teacher ID: {}", id, teacherId);
        
        Course course = courseRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        
        // Check if the teacher owns this course
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only delete their own courses");
        }
        
        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<CourseResponse> searchCoursesByTitle(String title) {
        log.debug("Searching courses by title: {}", title);
        
        List<Course> courses = courseRepository.findByTitleContaining(title);
        return courseMapper.toResponseList(courses);
    }
    
    // Internal method for other services
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
    }
}
