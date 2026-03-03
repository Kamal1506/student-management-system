package com.sms.service;

import com.sms.dto.CourseDTO;
import com.sms.model.Enrollment;
import com.sms.model.Course;
import com.sms.model.Teacher;
import com.sms.repository.CourseRepository;
import com.sms.repository.EnrollmentRepository;
import com.sms.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(course -> CourseDTO.builder()
                        .id(course.getId())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .code(course.getCode())
                        .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                        .teacherName(course.getTeacher() != null
                                ? course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName()
                                : null)
                        .build())
                .toList();
    }

    public CourseDTO createCourse(CourseDTO request) {
        if (request.getTeacherId() == null) {
            throw new RuntimeException("teacherId is required");
        }

        if (courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Course code already exists: " + request.getCode());
        }

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        Course saved = courseRepository.save(Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .code(request.getCode())
                .teacher(teacher)
                .build());

        return CourseDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .code(saved.getCode())
                .teacherId(saved.getTeacher() != null ? saved.getTeacher().getId() : null)
                .teacherName(saved.getTeacher() != null
                        ? saved.getTeacher().getFirstName() + " " + saved.getTeacher().getLastName()
                        : null)
                .build();
    }

    public CourseDTO updateCourse(Long courseId, CourseDTO request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        if (request.getTeacherId() == null) {
            throw new RuntimeException("teacherId is required");
        }

        if (request.getCode() != null
                && !request.getCode().equalsIgnoreCase(course.getCode())
                && courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Course code already exists: " + request.getCode());
        }

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.getTeacherId()));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCode(request.getCode());
        course.setTeacher(teacher);

        Course saved = courseRepository.save(course);
        return CourseDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .code(saved.getCode())
                .teacherId(saved.getTeacher() != null ? saved.getTeacher().getId() : null)
                .teacherName(saved.getTeacher() != null
                        ? saved.getTeacher().getFirstName() + " " + saved.getTeacher().getLastName()
                        : null)
                .build();
    }

    public String deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        courseRepository.delete(course);
        return "Course deleted successfully";
    }

    public Map<String, Object> enrollStudent(Long studentId, Long courseId) {
        Enrollment saved = enrollmentService.enroll(studentId, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("enrolledAt", saved.getEnrolledAt());
        response.put("active", saved.isActive());
        return response;
    }
}
