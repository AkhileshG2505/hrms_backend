package com.texlaculture.leave.entity;

import jakarta.persistence.*;
import lombok.*;


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


    @Column(name = "employee_id")
    private Long employeeId;
}
