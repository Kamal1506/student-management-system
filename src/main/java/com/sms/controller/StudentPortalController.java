package com.sms.controller;

import com.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentPortalController {

    private final StudentService studentService;

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyEnrolledCourses(authentication.getName()));
    }

    @GetMapping("/grades")
    public ResponseEntity<List<Map<String, Object>>> getMyGrades(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyGrades(authentication.getName()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Map<String, Object>>> getMyAssignments(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyAssignments(authentication.getName()));
    }

    @PutMapping("/assignments/{assignmentId}/complete")
    public ResponseEntity<Map<String, Object>> markComplete(
            @PathVariable Long assignmentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studentService.markAssignmentComplete(assignmentId, authentication.getName()));
    }
}
