package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.DashboardUserResponse;
import com.example.smart_fuel_management_system.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService service;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardController controller;

    @Test
    void getDashboard_shouldReturnDashboard_whenUserIsAuthenticated() {
        // given
        UUID userId = UUID.randomUUID();

        DashboardUserResponse dashboard =
                new DashboardUserResponse(5L, 12L);

        when(authentication.getName())
                .thenReturn(userId.toString());

        when(service.getDashboard(userId))
                .thenReturn(dashboard);

        // when
        ResponseEntity<DashboardUserResponse> result =
                controller.getDashboard(authentication);

        // then
        assertThat(result.getStatusCode().value())
                .isEqualTo(200);
        assertThat(result.getBody())
                .isSameAs(dashboard);
    }
}