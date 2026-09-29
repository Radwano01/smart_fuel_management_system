package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpStorageServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpStorageServiceImpl otpStorageService;


    @Test
    void saveEmailOtp_shouldStoreOtpWithTenMinuteExpiration() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        // when
        otpStorageService.saveEmailOtp(
                "user@gmail.com",
                "123456"
        );

        // then
        verify(valueOperations)
                .set(
                        "otp:email:user@gmail.com",
                        "123456",
                        Duration.ofMinutes(10)
                );
    }


    @Test
    void getEmailOtp_shouldReturnOtp_whenOtpExists() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("otp:email:user@gmail.com"))
                .thenReturn("123456");

        // when
        String result =
                otpStorageService.getEmailOtp(
                        "user@gmail.com"
                );

        // then
        assertThat(result)
                .isEqualTo("123456");
    }


    @Test
    void getEmailOtp_shouldThrowException_whenOtpDoesNotExist() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("otp:email:user@gmail.com"))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
                otpStorageService.getEmailOtp(
                        "user@gmail.com"
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("otp has expired");
    }


    @Test
    void deleteEmailOtp_shouldDeleteStoredOtp() {

        // given
        String email = "user@gmail.com";

        // when
        otpStorageService.deleteEmailOtp(email);

        // then
        verify(redisTemplate)
                .delete("otp:email:user@gmail.com");
    }


    @Test
    void savePhoneOtp_shouldStoreOtpWithTenMinuteExpiration() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        // when
        otpStorageService.savePhoneOtp(
                "905551234567",
                "123456"
        );

        // then
        verify(valueOperations)
                .set(
                        "otp:phone:905551234567",
                        "123456",
                        Duration.ofMinutes(10)
                );
    }


    @Test
    void getPhoneOtp_shouldReturnOtp_whenOtpExists() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("otp:phone:905551234567"))
                .thenReturn("123456");

        // when
        String result =
                otpStorageService.getPhoneOtp(
                        "905551234567"
                );

        // then
        assertThat(result)
                .isEqualTo("123456");
    }


    @Test
    void getPhoneOtp_shouldThrowException_whenOtpDoesNotExist() {

        // given
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("otp:phone:905551234567"))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
                otpStorageService.getPhoneOtp(
                        "905551234567"
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("otp has expired");
    }


    @Test
    void deletePhoneOtp_shouldDeleteStoredOtp() {

        // given
        String phone = "905551234567";

        // when
        otpStorageService.deletePhoneOtp(phone);

        // then
        verify(redisTemplate)
                .delete("otp:phone:905551234567");
    }
}
