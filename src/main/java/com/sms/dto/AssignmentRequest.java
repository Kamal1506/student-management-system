package com.sms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Long courseId;
}
