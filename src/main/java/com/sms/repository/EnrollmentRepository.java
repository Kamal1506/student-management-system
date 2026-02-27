package com.sms.repository;

import com.sms.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseIdAndActiveTrue(Long studentId, Long courseId);
    List<Enrollment> findByStudentIdAndActiveTrue(Long studentId);
    List<Enrollment> findByCourseIdAndActiveTrue(Long courseId);
}
