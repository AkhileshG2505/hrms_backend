package com.texlaculture.leave.service;

import com.texlaculture.leave.dto.CreateEmployeeRequest;
import com.texlaculture.leave.entity.Employee;
import com.texlaculture.leave.exception.LeaveRuleViolationException;
import com.texlaculture.leave.exception.ResourceNotFoundException;
import com.texlaculture.leave.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployee_savesNewEmployeeWithZeroLeaveTaken() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Priya Nair");
        request.setEmail("priya.nair@texlaculture.com");
        request.setAnnualLeaveQuota(20);

        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.createEmployee(request);

        assertThat(result.getName()).isEqualTo("Priya Nair");
        assertThat(result.getLeaveTaken()).isEqualTo(0);
        assertThat(result.getRemainingLeaveBalance()).isEqualTo(20);
    }

    @Test
    void createEmployee_throwsWhenEmailAlreadyExists() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Duplicate");
        request.setEmail("existing@texlaculture.com");
        request.setAnnualLeaveQuota(20);

        when(employeeRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(LeaveRuleViolationException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
