package com.sms.service;

import com.sms.model.*;
import com.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final ExamResultRepository examResultRepository;
    private final StudentRepository    studentRepository;
    private final CourseRepository     courseRepository;
    private final EmailService         emailService;

    public ExamResult submitGrade(Long studentId, Long courseId,
                                   double marks, double maxMarks,
                                   String grade, String examDate) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        ExamResult result = ExamResult.builder()
                .student(student)
                .course(course)
                .marks(marks)
                .maxMarks(maxMarks)
                .grade(grade)
                .examDate(examDate != null ? LocalDate.parse(examDate) : LocalDate.now())
                .build();

        ExamResult saved = examResultRepository.save(result);

        // ── Fire grade notification email ────────────────────────
        emailService.sendGradeNotification(
                student.getEmail(),
                student.getFirstName() + " " + student.getLastName(),
                course.getTitle(),
                marks,
                maxMarks,
                grade,
                examDate != null ? examDate : LocalDate.now().toString()
        );

        return saved;
    }
}