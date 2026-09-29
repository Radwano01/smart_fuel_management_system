package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.service.OtpStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpStorageServiceImpl implements OtpStorageService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String EMAIL_OTP_KEY = "otp:email:";
    private static final String PHONE_OTP_KEY = "otp:phone:";

    @Override
    public void saveEmailOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
                EMAIL_OTP_KEY + email,
                otp,
                Duration.ofMinutes(10)
        );
    }

    @Override
    public String getEmailOtp(String email) {
        String otp = (String) redisTemplate.opsForValue()
                .get(EMAIL_OTP_KEY + email);

        if(otp == null){
            throw new BadRequestException("otp has expired");
        }

        return otp;
    }

    @Override
    public void deleteEmailOtp(String email) {
        redisTemplate.delete(EMAIL_OTP_KEY + email);
    }

    @Override
    public void savePhoneOtp(String phone, String otp) {
        redisTemplate.opsForValue().set(
                PHONE_OTP_KEY + phone,
                otp,
                Duration.ofMinutes(10)
        );
    }

    @Override
    public String getPhoneOtp(String phone) {
        String otp = (String) redisTemplate.opsForValue()
                .get(PHONE_OTP_KEY + phone);

        if(otp == null){
            throw new BadRequestException("otp has expired");
        }

        return otp;
    }

    @Override
    public void deletePhoneOtp(String phone) {
        redisTemplate.delete(PHONE_OTP_KEY + phone);
    }
}
