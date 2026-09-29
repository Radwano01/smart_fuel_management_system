package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.DashboardAdminResponse;
import com.example.smart_fuel_management_system.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAdminControllerTest {

    @Mock
    private DashboardService service;

    @InjectMocks
    private DashboardAdminController controller;

    @Test
    void getDashboard_shouldReturnDashboard_whenCalled() {
        // given
        DashboardAdminResponse dashboard =
                DashboardAdminResponse.builder().build();

        when(service.getDashboard())
                .thenReturn(dashboard);

        // when
        ResponseEntity<DashboardAdminResponse> result =
                controller.getDashboard();

        // then
        assertThat(result.getStatusCode().value())
                .isEqualTo(200);
        assertThat(result.getBody())
                .isSameAs(dashboard);
    }
}