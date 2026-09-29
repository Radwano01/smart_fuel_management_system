package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.UpdateDTO;
import com.example.smart_fuel_management_system.dto.UserDTO;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    private Authentication authFor(String name) {
        return new UsernamePasswordAuthenticationToken(
                name,
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );
    }

    @Test
    void getUser_shouldReturnOk_whenAuthenticationNameIsValidUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDTO body = mock(UserDTO.class);
        when(userService.getUser(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/users")
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isOk());
        verify(userService).getUser(userId);
    }

    @Test
    void getUser_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/users")
                        .principal(authFor(malformed))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_shouldReturnNoContent_whenAuthenticationAndBodyAreValid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UpdateDTO request = new UpdateDTO("New Name");
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/users")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(userService).updateUser(eq(userId), any(UpdateDTO.class));
    }

    @Test
    void updateUser_shouldDelegateWithAuthenticationUserId_whenCalled() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UpdateDTO request = new UpdateDTO("Renamed");
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(
                patch("/api/v1/users")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        verify(userService).updateUser(eq(userId), any(UpdateDTO.class));
    }

    @Test
    void updateUser_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/users")
                        .principal(authFor(malformed))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"X\"}")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/users")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }
}