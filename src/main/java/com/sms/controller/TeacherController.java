package com.sms.controller;

import com.sms.dto.AssignmentRequest;
import com.sms.dto.GradeRequest;
import com.sms.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/assignments")
    public ResponseEntity<Map<String, Object>> createAssignment(
            @RequestBody AssignmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createAssignment(request, authentication.getName()));
    }

    @PostMapping("/grades")
    public ResponseEntity<Map<String, Object>> gradeStudent(
            @RequestBody GradeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.gradeStudent(request, authentication.getName()));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(teacherService.getMyAssignedCourses(authentication.getName()));
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<Map<String, Object>>> getStudentsInCourse(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teacherService.getStudentsInCourse(courseId, authentication.getName()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Map<String, Object>>> getMyAssignments(Authentication authentication) {
        return ResponseEntity.ok(teacherService.getMyAssignments(authentication.getName()));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Map<String, String>> deleteAssignment(
            @PathVariable Long assignmentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(Map.of("message", teacherService.deleteAssignment(assignmentId, authentication.getName())));
    }

    @GetMapping("/grades/{courseId}")
    public ResponseEntity<List<Map<String, Object>>> getGradesByCourse(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teacherService.getGradesByCourse(courseId, authentication.getName()));
    }
}
