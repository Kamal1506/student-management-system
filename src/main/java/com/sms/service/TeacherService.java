package com.sms.service;

import com.sms.dto.AssignmentRequest;
import com.sms.dto.GradeRequest;
import com.sms.dto.TeacherDTO;
import com.sms.enums.Role;
import com.sms.model.Assignment;
import com.sms.model.Course;
import com.sms.model.ExamResult;
import com.sms.model.Student;
import com.sms.model.Teacher;
import com.sms.model.User;
import com.sms.repository.AssignmentRepository;
import com.sms.repository.CourseRepository;
import com.sms.repository.EnrollmentRepository;
import com.sms.repository.ExamResultRepository;
import com.sms.repository.StudentRepository;
import com.sms.repository.TeacherRepository;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private static final String GMAIL_SUFFIX = "@gmail.com";

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExamResultRepository examResultRepository;

    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(teacher -> TeacherDTO.builder()
                        .id(teacher.getId())
                        .firstName(teacher.getFirstName())
                        .lastName(teacher.getLastName())
                        .email(teacher.getEmail())
                        .subject(teacher.getSubject())
                        .userId(teacher.getUser().getId())
                        .build())
                .toList();
    }

    public TeacherDTO createTeacher(TeacherDTO request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }
        String normalizedEmail = normalizeAndValidateGmail(request.getEmail());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("User must have TEACHER role");
        }

        if (teacherRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Teacher details already exist for userId: " + request.getUserId());
        }

        if (teacherRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Teacher email already exists: " + normalizedEmail);
        }

        Teacher saved = teacherRepository.save(Teacher.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(normalizedEmail)
                .subject(request.getSubject())
                .user(user)
                .build());

        return TeacherDTO.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .subject(saved.getSubject())
                .userId(saved.getUser().getId())
                .build();
    }

    public TeacherDTO updateTeacher(Long teacherId, TeacherDTO request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        String normalizedEmail = normalizeAndValidateGmail(request.getEmail());
        if (!normalizedEmail.equalsIgnoreCase(teacher.getEmail())
                && teacherRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Teacher email already exists: " + normalizedEmail);
        }

        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(normalizedEmail);
        teacher.setSubject(request.getSubject());

        Teacher saved = teacherRepository.save(teacher);
        return TeacherDTO.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .subject(saved.getSubject())
                .userId(saved.getUser().getId())
                .build();
    }

    public String deleteTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        teacherRepository.delete(teacher);
        return "Teacher deleted successfully";
    }

    public List<Map<String, Object>> getMyAssignedCourses(String username) {
        Teacher teacher = getTeacherByUsername(username);
        return courseRepository.findByTeacherId(teacher.getId())
                .stream()
                .map(course -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", course.getId());
                    row.put("title", course.getTitle());
                    row.put("description", course.getDescription());
                    row.put("code", course.getCode());
                    row.put("teacherId", course.getTeacher() != null ? course.getTeacher().getId() : null);
                    row.put("studentCount", enrollmentRepository.findByCourseIdAndActiveTrue(course.getId()).size());
                    return row;
                })
                .toList();
    }

    public List<Map<String, Object>> getMyAssignments(String username) {
        Teacher teacher = getTeacherByUsername(username);
        return assignmentRepository.findByTeacherId(teacher.getId())
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

    public String deleteAssignment(Long assignmentId, String username) {
        Teacher teacher = getTeacherByUsername(username);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (assignment.getTeacher() == null || !assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can only delete your own assignments");
        }

        assignmentRepository.delete(assignment);
        return "Assignment deleted successfully";
    }

    public List<Map<String, Object>> getGradesByCourse(Long courseId, String username) {
        Teacher teacher = getTeacherByUsername(username);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can only view grades for your own courses");
        }

        return examResultRepository.findByCourseId(courseId)
                .stream()
                .map(result -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", result.getId());
                    row.put("studentId", result.getStudent().getId());
                    row.put("studentName", result.getStudent().getFirstName() + " " + result.getStudent().getLastName());
                    row.put("courseId", result.getCourse().getId());
                    row.put("marks", result.getMarks());
                    row.put("maxMarks", result.getMaxMarks());
                    row.put("grade", result.getGrade());
                    row.put("examDate", result.getExamDate());
                    row.put("percentage", result.getPercentage());
                    return row;
                })
                .toList();
    }

    public List<Map<String, Object>> getStudentsInCourse(Long courseId, String username) {
        Teacher teacher = getTeacherByUsername(username);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You are not assigned to this course");
        }

        return enrollmentRepository.findByCourseIdAndActiveTrue(courseId)
                .stream()
                .map(enrollment -> {
                    Student student = enrollment.getStudent();
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", student.getId());
                    row.put("firstName", student.getFirstName());
                    row.put("lastName", student.getLastName());
                    row.put("email", student.getEmail());
                    row.put("phone", student.getPhone());
                    row.put("userId", student.getUser().getId());
                    return row;
                })
                .toList();
    }

    public Map<String, Object> createAssignment(AssignmentRequest request, String username) {
        Teacher teacher = getTeacherByUsername(username);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can only create assignments for your own courses");
        }

        Assignment saved = assignmentRepository.save(Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .course(course)
                .teacher(teacher)
                .build());

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("title", saved.getTitle());
        response.put("description", saved.getDescription());
        response.put("dueDate", saved.getDueDate());
        response.put("courseId", saved.getCourse().getId());
        response.put("teacherId", saved.getTeacher().getId());
        response.put("status", saved.getStatus().name());
        return response;
    }

    public Map<String, Object> gradeStudent(GradeRequest request, String username) {
        Teacher teacher = getTeacherByUsername(username);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + request.getCourseId()));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("You can only grade students in your own courses");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getStudentId()));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(
                student.getId(),
                course.getId()
        );

        if (!enrolled) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        ExamResult saved = examResultRepository.save(ExamResult.builder()
                .student(student)
                .course(course)
                .marks(request.getMarks())
                .maxMarks(request.getMaxMarks())
                .grade(request.getGrade())
                .examDate(request.getExamDate())
                .build());

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("studentId", student.getId());
        response.put("courseId", course.getId());
        response.put("marks", saved.getMarks());
        response.put("maxMarks", saved.getMaxMarks());
        response.put("grade", saved.getGrade());
        response.put("examDate", saved.getExamDate());
        response.put("percentage", saved.getPercentage());
        return response;
    }

    private Teacher getTeacherByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("Logged in user is not a TEACHER");
        }

        return teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for user: " + username));
    }

    private String normalizeAndValidateGmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!email.endsWith(GMAIL_SUFFIX)) {
            throw new RuntimeException("Email must end with @gmail.com");
        }
        return email;
    }
}
