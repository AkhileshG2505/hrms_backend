package com.texlaculture.leave.service;

import com.texlaculture.leave.dto.ApplyLeaveRequest;
import com.texlaculture.leave.entity.Employee;
import com.texlaculture.leave.entity.LeaveRequest;
import com.texlaculture.leave.entity.LeaveStatus;
import com.texlaculture.leave.exception.LeaveRuleViolationException;
import com.texlaculture.leave.exception.ResourceNotFoundException;
import com.texlaculture.leave.repository.EmployeeRepository;
import com.texlaculture.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    // Leave requests in these states "hold" days against the calendar,
    // so a new request can't overlap with them.
    private static final List<LeaveStatus> BLOCKING_STATUSES = List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);

    @Transactional
    public LeaveRequest applyForLeave(Long employeeId, ApplyLeaveRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No employee found with id: " + employeeId));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new LeaveRuleViolationException("endDate cannot be before startDate");
        }

        long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(
                request.getStartDate(), request.getEndDate()) + 1;

        // Rule 1: cannot request more days than remaining balance
        if (requestedDays > employee.getRemainingLeaveBalance()) {
            throw new LeaveRuleViolationException(
                    "Requested " + requestedDays + " day(s), but only "
                            + employee.getRemainingLeaveBalance() + " day(s) remain in the leave balance");
        }

        // Rule 2: cannot overlap with an existing pending/approved request
        List<LeaveRequest> existing = leaveRequestRepository
                .findByEmployeeIdAndStatusIn(employeeId, BLOCKING_STATUSES);

        boolean overlaps = existing.stream().anyMatch(lr ->
                !request.getEndDate().isBefore(lr.getStartDate())
                        && !request.getStartDate().isAfter(lr.getEndDate()));

        if (overlaps) {
            throw new LeaveRuleViolationException(
                    "Requested dates clash with an existing pending or approved leave request");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return leaveRequestRepository.save(leaveRequest);
    }

    public LeaveRequest getLeaveById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No leave request found with id: " + id));
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeavesForEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveId) {
        LeaveRequest leaveRequest = getLeaveById(leaveId);

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveRuleViolationException(
                    "Only a PENDING request can be approved. Current status: " + leaveRequest.getStatus());
        }

        Employee employee = leaveRequest.getEmployee();
        long days = leaveRequest.getNumberOfDays();

        // Re-check balance at approval time too - it may have changed
        // since the request was submitted (e.g. other leave approved meanwhile).
        if (days > employee.getRemainingLeaveBalance()) {
            throw new LeaveRuleViolationException(
                    "Cannot approve: employee only has " + employee.getRemainingLeaveBalance() + " day(s) left");
        }

        employee.setLeaveTaken(employee.getLeaveTaken() + (int) days);
        employeeRepository.save(employee);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest rejectLeave(Long leaveId) {
        LeaveRequest leaveRequest = getLeaveById(leaveId);

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveRuleViolationException(
                    "Only a PENDING request can be rejected. Current status: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * Ownership check used by the controller: an EMPLOYEE may only act on
     * their own leave requests / employee record. HR is exempt from this check.
     */
    public void assertOwnership(Long targetEmployeeId, Long callerEmployeeId) {
        if (callerEmployeeId == null || !callerEmployeeId.equals(targetEmployeeId)) {
            throw new AccessDeniedException("You may only access your own leave records");
        }
    }
}
