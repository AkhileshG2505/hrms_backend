package com.texlaculture.leave.controller;

import com.texlaculture.leave.dto.CreateEmployeeRequest;
import com.texlaculture.leave.dto.EmployeeResponse;
import com.texlaculture.leave.entity.AppUser;
import com.texlaculture.leave.entity.Employee;
import com.texlaculture.leave.entity.Role;
import com.texlaculture.leave.security.CurrentUserProvider;
import com.texlaculture.leave.service.EmployeeService;
import com.texlaculture.leave.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private final CurrentUserProvider currentUserProvider;

    // Only HR onboards new employees
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.from(employee));
    }

    // HR can view anyone; an EMPLOYEE can only view their own record
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.HR) {
            leaveService.assertOwnership(id, currentUser.getEmployeeId());
        }

        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(EmployeeResponse.from(employee));
    }
}
