package com.sms.service;

import com.sms.model.*;
import com.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository  enrollmentRepository;
    private final StudentRepository     studentRepository;
    private final CourseRepository      courseRepository;
    private final EmailService          emailService;

    public Enrollment enroll(Long studentId, Long courseId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        // Prevent duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDate.now())
                .active(true)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // ── Fire enrollment email ────────────────────────────────
        String teacherName = (course.getTeacher() != null)
                ? course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName()
                : "To be assigned";

        emailService.sendEnrollmentNotification(
                student.getEmail(),
                student.getFirstName() + " " + student.getLastName(),
                course.getTitle(),
                course.getCode(),
                teacherName
        );

        return saved;
    }
}