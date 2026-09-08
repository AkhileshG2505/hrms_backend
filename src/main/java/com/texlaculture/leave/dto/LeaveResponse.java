package com.texlaculture.leave.dto;

import com.texlaculture.leave.entity.LeaveRequest;
import com.texlaculture.leave.entity.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LeaveResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long numberOfDays;
    private String reason;
    private LeaveStatus status;

    public static LeaveResponse from(LeaveRequest leaveRequest) {
        return new LeaveResponse(
                leaveRequest.getId(),
                leaveRequest.getEmployee().getId(),
                leaveRequest.getEmployee().getName(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getNumberOfDays(),
                leaveRequest.getReason(),
                leaveRequest.getStatus()
        );
    }
}
