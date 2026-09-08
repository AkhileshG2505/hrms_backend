package com.texlaculture.leave.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Total leave days allotted for the year
    @Column(name = "annual_leave_quota", nullable = false)
    private int annualLeaveQuota;

    // Leave days already approved and consumed
    @Column(name = "leave_taken", nullable = false)
    @Builder.Default
    private int leaveTaken = 0;

    @Transient
    public int getRemainingLeaveBalance() {
        return annualLeaveQuota - leaveTaken;
    }
}
