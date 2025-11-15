package edu.platform.modules.course.service;

import edu.platform.modules.course.dto.request.LessonRequest;
import edu.platform.modules.course.dto.response.LessonResponse;
import edu.platform.modules.course.entity.Course;
import edu.platform.modules.course.entity.Lesson;
import edu.platform.modules.media.entity.MediaAsset;
import edu.platform.modules.media.service.MediaAssetService;
import edu.platform.common.exception.ResourceNotFoundException;
import edu.platform.modules.course.mapper.LessonMapper;
import edu.platform.modules.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {
    
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final CourseService courseService;
    private final MediaAssetService mediaAssetService;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public LessonResponse createLesson(LessonRequest request, Long teacherId) {
        log.debug("Creating lesson: {} for course: {}", request.getTitle(), request.getCourseId());
        
        Course course = courseService.getCourseEntityById(request.getCourseId());
        
        // Check if the teacher owns this course
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only create lessons for their own courses");
        }
        
        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setCourse(course);
        
        // Attach media assets if provided
        if (request.getMediaAssetIds() != null && !request.getMediaAssetIds().isEmpty()) {
            List<MediaAsset> mediaAssets = mediaAssetService.getMediaAssetEntitiesByIds(request.getMediaAssetIds());
            lesson.setMediaAssets(mediaAssets);
        }
        
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson created successfully with ID: {}", savedLesson.getId());
        
        return lessonMapper.toResponse(savedLesson);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public LessonResponse getLessonById(Long id) {
        log.debug("Fetching lesson by ID: {}", id);
        
        Lesson lesson = lessonRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + id));
        
        return lessonMapper.toResponse(lesson);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        log.debug("Fetching lessons for course: {}", courseId);
        
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return lessonMapper.toResponseList(lessons);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<LessonResponse> getLessonsByTeacher(Long teacherId) {
        log.debug("Fetching lessons by teacher: {}", teacherId);
        
        List<Lesson> lessons = lessonRepository.findByTeacherId(teacherId);
        return lessonMapper.toResponseList(lessons);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public LessonResponse updateLesson(Long id, LessonRequest request, Long teacherId) {
        log.debug("Updating lesson ID: {} by teacher: {}", id, teacherId);
        
        Lesson lesson = lessonRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + id));
        
        // Check if the teacher owns this lesson's course
        if (!lesson.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only update lessons for their own courses");
        }
        
        try {
            // Update fields
            lesson.setTitle(request.getTitle());
            lesson.setContent(request.getContent());
            lesson.setOrderIndex(request.getOrderIndex());
            
            // Update media assets if provided
            if (request.getMediaAssetIds() != null) {
                List<MediaAsset> mediaAssets = mediaAssetService.getMediaAssetEntitiesByIds(request.getMediaAssetIds());
                lesson.setMediaAssets(mediaAssets);
            }
            
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Lesson updated successfully with ID: {} (version: {})", savedLesson.getId(), savedLesson.getVersion());
            
            return lessonMapper.toResponse(savedLesson);
            
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for lesson ID: {}", id);
            throw new IllegalStateException("Lesson was modified by another user. Please refresh and try again.", e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void deleteLesson(Long id, Long teacherId) {
        log.debug("Deleting lesson ID: {} by teacher: {}", id, teacherId);
        
        Lesson lesson = lessonRepository.findByIdWithCourse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + id));
        
        // Check if the teacher owns this lesson's course
        if (!lesson.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only delete lessons for their own courses");
        }
        
        lessonRepository.delete(lesson);
        log.info("Lesson deleted successfully: {}", id);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public LessonResponse attachMediaToLesson(Long lessonId, Long mediaAssetId, Long teacherId) {
        log.debug("Attaching media asset {} to lesson {}", mediaAssetId, lessonId);
        
        Lesson lesson = lessonRepository.findByIdWithCourse(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
        
        // Check ownership
        if (!lesson.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only modify their own lessons");
        }
        
        MediaAsset mediaAsset = mediaAssetService.getMediaAssetEntityById(mediaAssetId);
        
        if (!lesson.getMediaAssets().contains(mediaAsset)) {
            lesson.getMediaAssets().add(mediaAsset);
            lessonRepository.save(lesson);
            log.info("Media asset {} attached to lesson {}", mediaAssetId, lessonId);
        }
        
        return lessonMapper.toResponse(lesson);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public LessonResponse detachMediaFromLesson(Long lessonId, Long mediaAssetId, Long teacherId) {
        log.debug("Detaching media asset {} from lesson {}", mediaAssetId, lessonId);
        
        Lesson lesson = lessonRepository.findByIdWithCourse(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
        
        // Check ownership
        if (!lesson.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only modify their own lessons");
        }
        
        lesson.getMediaAssets().removeIf(ma -> ma.getId().equals(mediaAssetId));
        lessonRepository.save(lesson);
        log.info("Media asset {} detached from lesson {}", mediaAssetId, lessonId);
        
        return lessonMapper.toResponse(lesson);
    }
}


