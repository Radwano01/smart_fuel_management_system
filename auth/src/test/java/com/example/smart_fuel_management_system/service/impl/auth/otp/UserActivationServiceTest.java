package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.AggregateType;
import com.example.smart_fuel_management_system.enums.EventType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.repository.OutBoxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivationServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private OutBoxRepository outBoxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserActivationService userActivationService;

    @Test
    void activateUser_shouldDoNothing_whenUserIsNotFullyVerified()
            throws Exception {

        // given
        PendingUser user = createUser();
        user.setVerifiedEmail(true);
        user.setVerifiedPhoneNumber(false);

        // when
        userActivationService.activateUser(user);

        // then
        verifyNoInteractions(
                authRepository,
                outBoxRepository,
                objectMapper,
                redisTemplate
        );
    }

    @Test
    void activateUser_shouldDoNothing_whenEmailAlreadyExists()
            throws Exception {

        // given
        PendingUser user = createUser();
        user.setVerifiedEmail(true);
        user.setVerifiedPhoneNumber(true);

        when(authRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        // when
        userActivationService.activateUser(user);

        // then
        verify(authRepository)
                .existsByEmail(user.getEmail());

        verify(authRepository, never())
                .save(any(Auth.class));

        verifyNoInteractions(
                outBoxRepository,
                objectMapper,
                redisTemplate
        );
    }

    @Test
    void activateUser_shouldCreateUserAndOutboxEvent_whenUserIsFullyVerified()
            throws Exception {

        // given
        PendingUser user = createUser();
        user.setVerifiedEmail(true);
        user.setVerifiedPhoneNumber(true);
        user.setRole(RoleType.USER);

        when(authRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("user-payload");

        // when
        userActivationService.activateUser(user);

        // then
        ArgumentCaptor<Auth> authCaptor =
                ArgumentCaptor.forClass(Auth.class);

        verify(authRepository)
                .save(authCaptor.capture());

        Auth auth = authCaptor.getValue();

        assertThat(auth.getId())
                .isEqualTo(user.getId());

        assertThat(auth.getEmail())
                .isEqualTo(user.getEmail());

        assertThat(auth.getPhoneNumber())
                .isEqualTo(user.getPhoneNumber());

        assertThat(auth.getRole())
                .isEqualTo(RoleType.USER);

        assertThat(auth.getAccountStatusType())
                .isEqualTo(AccountStatusType.ACTIVE);

        ArgumentCaptor<OutboxEvent> eventCaptor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outBoxRepository)
                .save(eventCaptor.capture());

        OutboxEvent event = eventCaptor.getValue();

        assertThat(event.getAggregateType())
                .isEqualTo(AggregateType.USER);

        assertThat(event.getEventType())
                .isEqualTo(EventType.USER_CREATED);

        verify(redisTemplate)
                .delete("pending_user:id:" + user.getId());

        verify(redisTemplate)
                .delete("pending_user:email:" + user.getEmail());

        verify(redisTemplate)
                .delete("pending_user:phone:" + user.getPhoneNumber());
    }

    @Test
    void activateUser_shouldCreateUserAndStationOutboxEvents_whenUserIsStation()
            throws Exception {

        // given
        PendingUser user = createUser();
        user.setVerifiedEmail(true);
        user.setVerifiedPhoneNumber(true);
        user.setRole(RoleType.STATION);

        when(authRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("payload");

        // when
        userActivationService.activateUser(user);

        // then
        ArgumentCaptor<Auth> authCaptor =
                ArgumentCaptor.forClass(Auth.class);

        verify(authRepository)
                .save(authCaptor.capture());

        assertThat(authCaptor.getValue().getRole())
                .isEqualTo(RoleType.STATION);

        ArgumentCaptor<OutboxEvent> eventCaptor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outBoxRepository, times(2))
                .save(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues())
                .extracting(OutboxEvent::getEventType)
                .containsExactlyInAnyOrder(
                        EventType.USER_CREATED,
                        EventType.STATION_CREATED
                );

        verify(redisTemplate)
                .delete("pending_user:id:" + user.getId());

        verify(redisTemplate)
                .delete("pending_user:email:" + user.getEmail());

        verify(redisTemplate)
                .delete("pending_user:phone:" + user.getPhoneNumber());
    }

    private PendingUser createUser() {
        PendingUser user = new PendingUser();

        user.setId(UUID.randomUUID());
        user.setEmail("user@gmail.com");
        user.setFullName("Radwan Rahmoun");
        user.setPhoneNumber("05331234567");
        user.setPassword("encodedPassword");
        user.setRole(RoleType.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
}
