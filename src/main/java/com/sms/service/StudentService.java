package com.sms.service;

import com.sms.dto.StudentDTO;
import com.sms.enums.AssignmentStatus;
import com.sms.enums.Role;
import com.sms.model.Assignment;
import com.sms.model.Enrollment;
import com.sms.model.ExamResult;
import com.sms.model.Student;
import com.sms.model.User;
import com.sms.repository.AssignmentRepository;
import com.sms.repository.EnrollmentRepository;
import com.sms.repository.ExamResultRepository;
import com.sms.repository.StudentRepository;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamResultRepository examResultRepository;
    private final AssignmentRepository assignmentRepository;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(student -> StudentDTO.builder()
                        .id(student.getId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .email(student.getEmail())
                        .phone(student.getPhone())
                        .userId(student.getUser().getId())
                        .build())
                .toList();
    }

    public StudentDTO createStudent(StudentDTO request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.STUDENT) {
            throw new RuntimeException("User must have STUDENT role");
        }

        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Student details already exist for userId: " + request.getUserId());
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Student email already exists: " + request.getEmail());
        }

        Student saved = studentRepository.save(Student.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .user(user)
                        .build());

        return StudentDTO.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .userId(saved.getUser().getId())
                .build();
    }

    public StudentDTO updateStudent(Long studentId, StudentDTO request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        if (request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(student.getEmail())
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Student email already exists: " + request.getEmail());
        }

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());

        Student saved = studentRepository.save(student);

        return StudentDTO.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .userId(saved.getUser().getId())
                .build();
    }

    public String deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        studentRepository.delete(student);
        return "Student deleted successfully";
    }

    public List<Map<String, Object>> getMyEnrolledCourses(String username) {
        Student student = getStudentByUsername(username);
        return enrollmentRepository.findByStudentIdAndActiveTrue(student.getId())
                .stream()
                .map(enrollment -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", enrollment.getCourse().getId());
                    row.put("title", enrollment.getCourse().getTitle());
                    row.put("description", enrollment.getCourse().getDescription());
                    row.put("code", enrollment.getCourse().getCode());
                    row.put("teacherId", enrollment.getCourse().getTeacher() != null
                            ? enrollment.getCourse().getTeacher().getId()
                            : null);
                    return row;
                })
                .toList();
    }

    public List<Map<String, Object>> getMyGrades(String username) {
        Student student = getStudentByUsername(username);
        return examResultRepository.findByStudentId(student.getId())
                .stream()
                .map(result -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("courseTitle", result.getCourse().getTitle());
                    row.put("marks", result.getMarks());
                    row.put("maxMarks", result.getMaxMarks());
                    row.put("grade", result.getGrade());
                    row.put("percentage", result.getPercentage());
                    row.put("examDate", result.getExamDate());
                    return row;
                })
                .toList();
    }

    public List<Map<String, Object>> getMyAssignments(String username) {
        Student student = getStudentByUsername(username);

        List<Long> enrolledCourseIds = enrollmentRepository.findByStudentIdAndActiveTrue(student.getId())
                .stream()
                .map(Enrollment::getCourse)
                .map(course -> course.getId())
                .toList();

        if (enrolledCourseIds.isEmpty()) {
            return List.of();
        }

        return assignmentRepository.findByCourseIdIn(enrolledCourseIds)
                .stream()
                .map(assignment -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", assignment.getId());
                    row.put("title", assignment.getTitle());
                    row.put("description", assignment.getDescription());
                    row.put("dueDate", assignment.getDueDate());
                    row.put("courseId", assignment.getCourse().getId());
                    row.put("courseTitle", assignment.getCourse().getTitle());
                    row.put("status", assignment.getStatus().name());
                    return row;
                })
                .toList();
    }

    public Map<String, Object> markAssignmentComplete(Long assignmentId, String username) {
        Student student = getStudentByUsername(username);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(
                student.getId(),
                assignment.getCourse().getId()
        );

        if (!enrolled) {
            throw new RuntimeException("Student is not enrolled in this assignment's course");
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        Assignment saved = assignmentRepository.save(assignment);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("status", saved.getStatus().name());
        response.put("message", "Assignment marked as complete");
        return response;
    }

    public Student getStudentByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (user.getRole() != Role.STUDENT) {
            throw new RuntimeException("Logged in user is not a STUDENT");
        }

        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found for user: " + username));
    }
}
