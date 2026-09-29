package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {
    StationEmployeeResponse getEmployeeInfo(UUID employeeId);
    EmployeeDetailsResponse getEmployeeDetails(UUID employeeId);
    void update(UUID employeeId, UpdateEmployeeRequest request);
    void update(UUID employeeId, UpdateUserRequest request);
    Page<EmployeeResponse> search(
            AccountSearchType searchType,
            String search,
            AccountStatusType status,
            Pageable pageable);
}
