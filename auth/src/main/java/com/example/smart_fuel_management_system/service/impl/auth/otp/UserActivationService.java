package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.dto.SaveStationUser;
import com.example.smart_fuel_management_system.dto.SaveUser;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.AggregateType;
import com.example.smart_fuel_management_system.enums.EventType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.repository.OutBoxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivationService {

    private final AuthRepository authRepository;
    private final OutBoxRepository outBoxRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_KEY = "pending_user:id:";
    private static final String EMAIL_KEY = "pending_user:email:";
    private static final String PHONE_KEY = "pending_user:phone:";

    public void activateUser(PendingUser user) throws JsonProcessingException {

        if (!user.isVerifiedEmail() || !user.isVerifiedPhoneNumber()) {
            return;
        }

        if (authRepository.existsByEmail(user.getEmail())) {
            return;
        }

        if(user.getRole() == RoleType.STATION){
            activateStationUser(user);
            return;
        }

        Auth auth = createAuth(user, RoleType.USER);
        authRepository.save(auth);

        OutboxEvent event = new OutboxEvent(
                AggregateType.USER,
                EventType.USER_CREATED,
                objectMapper.writeValueAsString(
                        new SaveUser(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        )
                )
        );

        outBoxRepository.save(event);

        deletePendingUser(user);
    }

    public void activateStationUser(PendingUser user)
            throws JsonProcessingException {

        Auth auth = createAuth(user, RoleType.STATION);
        authRepository.save(auth);

        // Create User in User Service
        OutboxEvent userEvent = new OutboxEvent(
                AggregateType.USER,
                EventType.USER_CREATED,
                objectMapper.writeValueAsString(
                        new SaveUser(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        )
                )
        );

        // Create StationEmployee in Station Service
        OutboxEvent stationEmployeeEvent = new OutboxEvent(
                AggregateType.STATION,
                EventType.STATION_CREATED,
                objectMapper.writeValueAsString(
                        new SaveStationUser(
                                user.getId()
                        )
                ));

        outBoxRepository.save(userEvent);
        outBoxRepository.save(stationEmployeeEvent);

        deletePendingUser(user);
    }

    private Auth createAuth(PendingUser user, RoleType role) {
        return new Auth(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPassword(),
                role,
                AccountStatusType.ACTIVE
        );
    }

    private void deletePendingUser(PendingUser user) {
        redisTemplate.delete(USER_KEY + user.getId());
        redisTemplate.delete(EMAIL_KEY + user.getEmail());
        redisTemplate.delete(PHONE_KEY + user.getPhoneNumber());
    }
}