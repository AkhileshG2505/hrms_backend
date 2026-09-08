package com.texlaculture.leave.service;

import com.texlaculture.leave.dto.CreateEmployeeRequest;
import com.texlaculture.leave.entity.Employee;
import com.texlaculture.leave.exception.LeaveRuleViolationException;
import com.texlaculture.leave.exception.ResourceNotFoundException;
import com.texlaculture.leave.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public Employee createEmployee(CreateEmployeeRequest request) {
        employeeRepository.findByEmail(request.getEmail()).ifPresent(e -> {
            throw new LeaveRuleViolationException("An employee with this email already exists");
        });

        Employee employee = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .annualLeaveQuota(request.getAnnualLeaveQuota())
                .leaveTaken(0)
                .build();

        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No employee found with id: " + id));
    }
}
