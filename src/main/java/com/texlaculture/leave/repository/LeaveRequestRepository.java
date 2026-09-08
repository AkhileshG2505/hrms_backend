package com.texlaculture.leave.repository;

import com.texlaculture.leave.entity.LeaveRequest;
import com.texlaculture.leave.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatusIn(Long employeeId, List<LeaveStatus> statuses);
}
