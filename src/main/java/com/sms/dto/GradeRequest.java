package com.sms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GradeRequest {
    private Long studentId;
    private Long courseId;
    private Double marks;
    private Double maxMarks;
    private String grade;
    private LocalDate examDate;
}
