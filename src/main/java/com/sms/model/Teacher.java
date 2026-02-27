package com.sms.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 100)
    private String subject; // e.g. "Mathematics", "Physics"

    // Each teacher links to exactly one login account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // A teacher can manage many courses
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Course> courses = new ArrayList<>();
}