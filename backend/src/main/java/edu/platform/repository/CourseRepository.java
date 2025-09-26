package edu.platform.repository;

import edu.platform.entity.Course;
import edu.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByTeacher(User teacher);
    
    List<Course> findByTeacherId(Long teacherId);
    
    @Query("SELECT c FROM Course c WHERE c.title LIKE %:title%")
    List<Course> findByTitleContaining(@Param("title") String title);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE c.id = :id")
    Optional<Course> findByIdWithTeacher(@Param("id") Long id);
    
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher")
    List<Course> findAllWithTeacher();
}
