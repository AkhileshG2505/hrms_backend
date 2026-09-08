package com.texlaculture.leave.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplyLeaveRequest {

    @NotNull(message = "startDate is required")
    @FutureOrPresent(message = "startDate cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    @NotBlank(message = "reason is required")
    private String reason;
}
