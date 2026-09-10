package com.texlaculture.leave.controller;

import com.texlaculture.leave.dto.ApplyLeaveRequest;
import com.texlaculture.leave.dto.LeaveResponse;
import com.texlaculture.leave.entity.AppUser;
import com.texlaculture.leave.entity.LeaveRequest;
import com.texlaculture.leave.entity.Role;
import com.texlaculture.leave.security.CurrentUserProvider;
import com.texlaculture.leave.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final CurrentUserProvider currentUserProvider;


    @PostMapping("/employees/{employeeId}")
    public ResponseEntity<LeaveResponse> applyForLeave(@PathVariable Long employeeId,
                                                         @Valid @RequestBody ApplyLeaveRequest request) {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.HR) {
            leaveService.assertOwnership(employeeId, currentUser.getEmployeeId());
        }

        LeaveRequest leaveRequest = leaveService.applyForLeave(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveResponse.from(leaveRequest));
    }


    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getLeaves() {
        AppUser currentUser = currentUserProvider.getCurrentUser();

        List<LeaveRequest> leaves = currentUser.getRole() == Role.HR
                ? leaveService.getAllLeaves()
                : leaveService.getLeavesForEmployee(currentUser.getEmployeeId());

        List<LeaveResponse> response = leaves.stream().map(LeaveResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeave(@PathVariable Long id) {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        LeaveRequest leaveRequest = leaveService.getLeaveById(id);

        if (currentUser.getRole() != Role.HR
                && !leaveRequest.getEmployee().getId().equals(currentUser.getEmployeeId())) {
            throw new AccessDeniedException("You may only view your own leave requests");
        }

        return ResponseEntity.ok(LeaveResponse.from(leaveRequest));
    }


    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.approveLeave(id);
        return ResponseEntity.ok(LeaveResponse.from(leaveRequest));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.rejectLeave(id);
        return ResponseEntity.ok(LeaveResponse.from(leaveRequest));
    }
}
