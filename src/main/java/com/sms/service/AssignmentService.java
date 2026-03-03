package com.sms.service;

import com.sms.model.*;
import com.sms.repository.*;
import com.sms.enums.AssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository  assignmentRepository;
    private final CourseRepository      courseRepository;
    private final EnrollmentRepository  enrollmentRepository;
    private final EmailService          emailService;

    public Assignment createAssignment(String title, String description,
                                        Long courseId, String dueDate) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        Assignment assignment = Assignment.builder()
                .title(title)
                .description(description)
                .course(course)
                .teacher(course.getTeacher())
                .dueDate(dueDate != null ? java.time.LocalDate.parse(dueDate) : null)
                .status(AssignmentStatus.PENDING)
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        // ── Notify all enrolled students ─────────────────────────
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndActiveTrue(courseId);
        for (Enrollment e : enrollments) {
            Student s = e.getStudent();
            emailService.sendAssignmentNotification(
                    s.getEmail(),
                    s.getFirstName() + " " + s.getLastName(),
                    title,
                    course.getTitle(),
                    dueDate != null ? dueDate : "No due date",
                    description
            );
        }

        return saved;
    }

    public Assignment markComplete(Long assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        a.setStatus(AssignmentStatus.COMPLETED);
        return assignmentRepository.save(a);
    }
}
