package com.sms.controller;

import com.sms.dto.EnrollmentRequest;
import com.sms.dto.StudentDTO;
import com.sms.dto.TeacherDTO;
import com.sms.repository.EnrollmentRepository;
import com.sms.service.CourseService;
import com.sms.service.StudentService;
import com.sms.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin endpoint is working");
    }

    @GetMapping("/students")
    public ResponseEntity<java.util.List<StudentDTO>> getAllStudentsForAdmin() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getAdminStats() {
        Map<String, Long> stats = Map.of(
                "totalStudents", (long) studentService.getAllStudents().size(),
                "totalTeachers", (long) teacherService.getAllTeachers().size(),
                "totalCourses", (long) courseService.getAllCourses().size(),
                "totalEnrollments", enrollmentRepository.count()
        );
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/students")
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody StudentDTO request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", studentService.deleteStudent(id)));
    }

    @PostMapping("/teachers")
    public ResponseEntity<TeacherDTO> createTeacher(@RequestBody TeacherDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createTeacher(request));
    }

    @GetMapping("/teachers")
    public ResponseEntity<java.util.List<TeacherDTO>> getAllTeachersForAdmin() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<TeacherDTO> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Map<String, String>> deleteTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", teacherService.deleteTeacher(id)));
    }

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enrollStudent(@RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.enrollStudent(request.getStudentId(), request.getCourseId()));
    }
}
