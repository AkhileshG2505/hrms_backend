package com.texlaculture.leave.service;

import com.texlaculture.leave.dto.ApplyLeaveRequest;
import com.texlaculture.leave.entity.Employee;
import com.texlaculture.leave.entity.LeaveRequest;
import com.texlaculture.leave.entity.LeaveStatus;
import com.texlaculture.leave.exception.LeaveRuleViolationException;
import com.texlaculture.leave.exception.ResourceNotFoundException;
import com.texlaculture.leave.repository.EmployeeRepository;
import com.texlaculture.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .name("Asha Rao")
                .email("asha.rao@texlaculture.com")
                .annualLeaveQuota(20)
                .leaveTaken(0)
                .build();
    }

    @Test
    void applyForLeave_succeedsWhenWithinBalanceAndNoOverlap() {
        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(7)); // 3 days
        request.setReason("Family trip");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeIdAndStatusIn(eq(1L), any())).thenReturn(List.of());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.applyForLeave(1L, request);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(result.getNumberOfDays()).isEqualTo(3);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void applyForLeave_throwsWhenRequestExceedsRemainingBalance() {
        employee.setLeaveTaken(18); // only 2 days left

        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5)); // 5 days requested
        request.setReason("Vacation");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyForLeave(1L, request))
                .isInstanceOf(LeaveRuleViolationException.class)
                .hasMessageContaining("only 2 day(s) remain");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyForLeave_throwsWhenDatesOverlapExistingRequest() {
        LeaveRequest existing = LeaveRequest.builder()
                .id(10L)
                .employee(employee)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .status(LeaveStatus.PENDING)
                .build();

        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(8)); // overlaps with existing (5-10)
        request.setEndDate(LocalDate.now().plusDays(12));
        request.setReason("Overlapping request");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeIdAndStatusIn(eq(1L), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> leaveService.applyForLeave(1L, request))
                .isInstanceOf(LeaveRuleViolationException.class)
                .hasMessageContaining("clash");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyForLeave_throwsWhenEndDateBeforeStartDate() {
        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReason("Bad dates");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyForLeave(1L, request))
                .isInstanceOf(LeaveRuleViolationException.class)
                .hasMessageContaining("endDate cannot be before startDate");
    }

    @Test
    void applyForLeave_throwsWhenEmployeeDoesNotExist() {
        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReason("Trip");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.applyForLeave(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void approveLeave_reducesBalanceAndMarksApproved() {
        LeaveRequest pending = LeaveRequest.builder()
                .id(5L)
                .employee(employee)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3)) // 3 days
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(pending));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.approveLeave(5L);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(employee.getLeaveTaken()).isEqualTo(3);
        assertThat(employee.getRemainingLeaveBalance()).isEqualTo(17);
    }

    @Test
    void approveLeave_throwsWhenRequestIsNotPending() {
        LeaveRequest alreadyApproved = LeaveRequest.builder()
                .id(6L)
                .employee(employee)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findById(6L)).thenReturn(Optional.of(alreadyApproved));

        assertThatThrownBy(() -> leaveService.approveLeave(6L))
                .isInstanceOf(LeaveRuleViolationException.class)
                .hasMessageContaining("Only a PENDING request can be approved");
    }

    @Test
    void rejectLeave_marksRequestRejectedWithoutTouchingBalance() {
        LeaveRequest pending = LeaveRequest.builder()
                .id(7L)
                .employee(employee)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.rejectLeave(7L);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(employee.getLeaveTaken()).isEqualTo(0);
        verify(employeeRepository, never()).save(any());
    }
}
