package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.User;
import com.example.smart_fuel_management_system.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select new com.example.smart_fuel_management_system.dto.UserResponse(u.id, u.fullName) from User u where u.id = :id")
    UserResponse findFullNameById(@Param("id") UUID id);
    @Query("select new com.example.smart_fuel_management_system.dto.UserResponse(u.id, u.fullName) from User u where u.fullName = :search")
    List<UserResponse> findByFullNameContainingIgnoreCase(@Param("search") String search);
    Page<User> findByFullNameContainingIgnoreCase(String search, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCase(String search, Pageable pageable);
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
