package com.texlaculture.leave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotNull(message = "annualLeaveQuota is required")
    @Min(value = 0, message = "annualLeaveQuota cannot be negative")
    private Integer annualLeaveQuota;
}
