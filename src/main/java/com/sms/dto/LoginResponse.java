package com.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;   // The JWT token — frontend stores this in localStorage
    private String role;    // "ADMIN" | "TEACHER" | "STUDENT" — used to redirect to correct dashboard
    private String username;
}