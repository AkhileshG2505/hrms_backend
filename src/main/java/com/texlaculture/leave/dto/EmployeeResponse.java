package com.texlaculture.leave.dto;

import com.texlaculture.leave.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private int annualLeaveQuota;
    private int leaveTaken;
    private int remainingLeaveBalance;

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getAnnualLeaveQuota(),
                employee.getLeaveTaken(),
                employee.getRemainingLeaveBalance()
        );
    }
}
