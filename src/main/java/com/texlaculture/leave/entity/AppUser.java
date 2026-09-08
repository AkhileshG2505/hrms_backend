package com.texlaculture.leave.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Login identity, kept separate from Employee on purpose.
 * HR staff don't need an Employee record (no leave balance to track),
 * and this keeps authentication concerns out of the HR/business entities.
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt-hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Links this login to their Employee record, so an EMPLOYEE user
    // can only see/apply for their own leave. Null for HR users.
    @Column(name = "employee_id")
    private Long employeeId;
}
