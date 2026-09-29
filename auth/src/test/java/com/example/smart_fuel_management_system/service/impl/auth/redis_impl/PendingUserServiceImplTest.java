package com.example.smart_fuel_management_system.service.impl.auth.redis_impl;

import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingUserServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PendingUserServiceImpl service;

    @Test
    void save_shouldStoreUserByIdEmailAndPhone() {

        // given
        PendingUser user = mock(PendingUser.class);

        UUID userId = UUID.randomUUID();
        String email = "user@gmail.com";
        String phone = "5331234567";

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(user.getPhoneNumber()).thenReturn(phone);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        service.save(user);

        // then
        verify(valueOperations)
                .set(
                        "pending_user:id:" + userId,
                        user
                );

        verify(valueOperations)
                .set(
                        "pending_user:email:" + email,
                        userId.toString()
                );

        verify(valueOperations)
                .set(
                        "pending_user:phone:" + phone,
                        userId.toString()
                );
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {

        // given
        String email = "user@gmail.com";
        UUID userId = UUID.randomUUID();

        PendingUser user = mock(PendingUser.class);
        Object cachedUser = new Object();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:email:" + email
        )).thenReturn(userId.toString());
        when(valueOperations.get(
                "pending_user:id:" + userId
        )).thenReturn(cachedUser);
        when(objectMapper.convertValue(
                cachedUser,
                PendingUser.class
        )).thenReturn(user);

        // when
        PendingUser result =
                service.findByEmail(email);

        // then
        assertThat(result).isSameAs(user);
    }

    @Test
    void findByEmail_shouldThrowException_whenEmailDoesNotExist() {

        // given
        String email = "user@gmail.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:email:" + email
        )).thenReturn(null);

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> service.findByEmail(email)
        );
    }

    @Test
    void findByEmail_shouldThrowException_whenUserCacheExpired() {

        // given
        String email = "user@gmail.com";
        UUID userId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:email:" + email
        )).thenReturn(userId.toString());
        when(valueOperations.get(
                "pending_user:id:" + userId
        )).thenReturn(null);

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> service.findByEmail(email)
        );
    }

    @Test
    void findByPhone_shouldReturnUser_whenUserExists() {

        // given
        String phone = "5331234567";
        UUID userId = UUID.randomUUID();

        PendingUser user = mock(PendingUser.class);
        Object cachedUser = new Object();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:phone:" + phone
        )).thenReturn(userId.toString());
        when(valueOperations.get(
                "pending_user:id:" + userId
        )).thenReturn(cachedUser);
        when(objectMapper.convertValue(
                cachedUser,
                PendingUser.class
        )).thenReturn(user);

        // when
        PendingUser result =
                service.findByPhone(phone);

        // then
        assertThat(result).isSameAs(user);
    }

    @Test
    void findByPhone_shouldThrowException_whenPhoneDoesNotExist() {

        // given
        String phone = "5331234567";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:phone:" + phone
        )).thenReturn(null);

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> service.findByPhone(phone)
        );
    }

    @Test
    void findByPhone_shouldThrowException_whenUserCacheExpired() {

        // given
        String phone = "5331234567";
        UUID userId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:phone:" + phone
        )).thenReturn(userId.toString());
        when(valueOperations.get(
                "pending_user:id:" + userId
        )).thenReturn(null);

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> service.findByPhone(phone)
        );
    }

    @Test
    void existsEmail_shouldReturnTrue_whenEmailExists() {

        // given
        String email = "user@gmail.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:email:" + email
        )).thenReturn(UUID.randomUUID().toString());

        // when
        boolean result =
                service.existsEmail(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByPhone_shouldReturnTrue_whenPhoneExists() {

        // given
        String phone = "5331234567";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:phone:" + phone
        )).thenReturn(UUID.randomUUID().toString());

        // when
        boolean result =
                service.existsByPhone(phone);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void validateEmail_shouldThrowException_whenEmailIsEmpty() {

        // given
        String email = " ";

        // when & then
        assertThrows(
                BadRequestException.class,
                () -> service.validateEmail(email)
        );

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void validateEmail_shouldThrowException_whenEmailAlreadyExists() {

        // given
        String email = "user@gmail.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:email:" + email
        )).thenReturn(UUID.randomUUID().toString());

        // when & then
        assertThrows(
                EntityExistsException.class,
                () -> service.validateEmail(email)
        );
    }

    @Test
    void validatePhone_shouldThrowException_whenPhoneIsEmpty() {

        // given
        String phone = " ";

        // when & then
        assertThrows(
                BadRequestException.class,
                () -> service.validatePhone(phone)
        );

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void validatePhone_shouldThrowException_whenPhoneAlreadyExists() {

        // given
        String phone = "5331234567";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_user:phone:" + phone
        )).thenReturn(UUID.randomUUID().toString());

        // when & then
        assertThrows(
                EntityExistsException.class,
                () -> service.validatePhone(phone)
        );
    }
}