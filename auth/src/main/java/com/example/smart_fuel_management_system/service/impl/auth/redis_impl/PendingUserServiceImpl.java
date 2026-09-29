package com.example.smart_fuel_management_system.service.impl.auth.redis_impl;

import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.service.PendingUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PendingUserServiceImpl implements PendingUserService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_KEY = "pending_user:id:";
    private static final String EMAIL_KEY = "pending_user:email:";
    private static final String PHONE_KEY = "pending_user:phone:";

    @Override
    public void save(PendingUser user) {
        redisTemplate.opsForValue().set(
                USER_KEY + user.getId(),
                user
        );

        redisTemplate.opsForValue().set(
                EMAIL_KEY + user.getEmail(),
                user.getId().toString()
        );

        redisTemplate.opsForValue().set(
                PHONE_KEY + user.getPhoneNumber(),
                user.getId().toString()
        );
    }

    @Override
    public PendingUser findByEmail(String email) {

        String id = (String) redisTemplate.opsForValue()
                .get(EMAIL_KEY + email);

        if (id == null) {
            throw new EntityNotFoundException("User not found");
        }

        Object value = redisTemplate.opsForValue()
                .get(USER_KEY + id);

        if (value == null) {
            throw new EntityNotFoundException("User cache expired");
        }

        return objectMapper.convertValue(value, PendingUser.class);
    }

    @Override
    public PendingUser findByPhone(String phone) {

        String id = (String) redisTemplate.opsForValue()
                .get(PHONE_KEY + phone);

        if (id == null) {
            throw new EntityNotFoundException("User not found");
        }

        Object value = redisTemplate.opsForValue()
                .get(USER_KEY + id);

        if (value == null) {
            throw new EntityNotFoundException("User cache expired");
        }

        return objectMapper.convertValue(value, PendingUser.class);
    }

    @Override
    public boolean existsEmail(String email) {
        return redisTemplate.opsForValue()
                .get(EMAIL_KEY + email) != null;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return redisTemplate.opsForValue()
                .get(PHONE_KEY + phone) != null;
    }

    @Override
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email empty");
        }

        if (existsEmail(email)) {
            throw new EntityExistsException(
                    "Email already is valid in verification stage"
            );
        }
    }

    @Override
    public void validatePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("Phone empty");
        }

        if (existsByPhone(phoneNumber)) {
            throw new EntityExistsException(
                    "Phone Number is already valid in verification stage"
            );
        }
    }
}