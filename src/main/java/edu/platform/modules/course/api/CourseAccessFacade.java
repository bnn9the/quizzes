package edu.platform.modules.course.api;

import edu.platform.modules.course.entity.Course;

public interface CourseAccessFacade {

    Course getCourseEntityById(Long id);

    void validateTeacherOwnership(Long courseId, Long teacherId);
}
