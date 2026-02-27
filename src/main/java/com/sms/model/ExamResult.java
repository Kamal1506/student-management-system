package com.sms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many results can belong to one student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    // Many results can belong to one course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

    @Column(nullable = false)
    private Double marks; // Score the student got

    @Column(nullable = false)
    private Double maxMarks; // Maximum possible score

    @Column(length = 5)
    private String grade; // e.g. "A", "B+", "C"

    @Column(nullable = false)
    @Builder.Default
    private LocalDate examDate = LocalDate.now();

    // Convenience method — calculates percentage automatically
    @Transient
    public double getPercentage() {
        if (maxMarks == null || maxMarks == 0) return 0;
        return (marks / maxMarks) * 100;
    }
}