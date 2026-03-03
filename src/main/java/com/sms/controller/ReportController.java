package com.sms.controller;

import com.sms.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfReportService pdfReportService;

    // ── Student Report Card (Admin or the student themselves) ────────
    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<byte[]> studentReport(@PathVariable Long id) throws Exception {
        byte[] pdf = pdfReportService.generateStudentReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"student-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Course Report (Admin or Teacher) ─────────────────────────────
    @GetMapping("/course/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<byte[]> courseReport(@PathVariable Long id) throws Exception {
        byte[] pdf = pdfReportService.generateCourseReport(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"course-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Admin Full Summary Report ─────────────────────────────────────
    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> adminSummary() throws Exception {
        byte[] pdf = pdfReportService.generateAdminSummaryReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"school-summary-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}