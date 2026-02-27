package com.sms.model;

import com.sms.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt hashed — never store plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN | TEACHER | STUDENT

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}