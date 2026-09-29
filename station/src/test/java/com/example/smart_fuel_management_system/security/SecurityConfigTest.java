package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.ProbeController.class)
@Import({
        SecurityConfig.class,
        JWTAuthenticationFilter.class,
        JWTAuthEntryPoint.class,
        RateLimitingFilter.class,
        SecurityConfigTest.ProbeController.class
})
class SecurityConfigTest {

    private static final String STATION_TOKEN = "station-token";
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter rateLimiter;

    private UUID employeeId;
    private String adminId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        adminId = "admin-1";

        lenient().when(jwtService.validateToken(STATION_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(STATION_TOKEN)).thenReturn("STATION");
        lenient().when(jwtService.extractEmployeeId(STATION_TOKEN)).thenReturn(employeeId);

        lenient().when(jwtService.validateToken(ADMIN_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(ADMIN_TOKEN)).thenReturn("ADMIN");
        lenient().when(jwtService.extractSubject(ADMIN_TOKEN)).thenReturn(adminId);

        lenient().when(jwtService.validateToken(SERVICE_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(SERVICE_TOKEN)).thenReturn("SERVICE");
        lenient().when(jwtService.extractIssuer(SERVICE_TOKEN)).thenReturn("auth-service");
        lenient().when(jwtService.extractAudience(SERVICE_TOKEN)).thenReturn("station-service");
        lenient().when(jwtService.extractSubject(SERVICE_TOKEN)).thenReturn("auth-service");

        lenient().when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/v1/internal/probe")
        ResponseEntity<String> internal() {
            return ResponseEntity.ok("internal");
        }

        @GetMapping("/api/v1/admin/probe")
        ResponseEntity<String> admin() {
            return ResponseEntity.ok("admin");
        }

        @GetMapping("/api/v1/stations/probe")
        ResponseEntity<String> stations() {
            return ResponseEntity.ok("stations");
        }

        @GetMapping("/any/probe")
        ResponseEntity<String> any() {
            return ResponseEntity.ok("any");
        }

        @PostMapping("/api/v1/admin/probe")
        ResponseEntity<String> adminPost() {
            return ResponseEntity.ok("posted");
        }
    }

    private MockHttpServletRequestBuilder asStation(String path) {
        return get(path).header("Authorization", "Bearer " + STATION_TOKEN);
    }

    private MockHttpServletRequestBuilder asAdmin(String path) {
        return get(path).header("Authorization", "Bearer " + ADMIN_TOKEN);
    }

    private MockHttpServletRequestBuilder asService(String path) {
        return get(path).header("Authorization", "Bearer " + SERVICE_TOKEN);
    }

    @Test
    void internalEndpoint_shouldReturnOk_whenAuthorityIsService() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asService("/api/v1/internal/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void internalEndpoint_shouldReturnForbidden_whenAuthorityIsAdmin() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asAdmin("/api/v1/internal/probe"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_shouldReturnForbidden_whenAuthorityIsStation() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asStation("/api/v1/internal/probe"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_shouldReturnUnauthorized_whenUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/internal/probe"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_shouldReturnOk_whenAuthorityIsAdmin() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asAdmin("/api/v1/admin/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldReturnForbidden_whenAuthorityIsService() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asService("/api/v1/admin/probe"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldReturnForbidden_whenAuthorityIsStation() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asStation("/api/v1/admin/probe"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldReturnUnauthorized_whenUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/probe"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void stationsEndpoint_shouldReturnOk_whenAuthorityIsStation() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asStation("/api/v1/stations/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void stationsEndpoint_shouldReturnOk_whenAuthorityIsAdmin() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asAdmin("/api/v1/stations/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void stationsEndpoint_shouldReturnForbidden_whenAuthorityIsService() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asService("/api/v1/stations/probe"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void stationsEndpoint_shouldReturnUnauthorized_whenUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/stations/probe"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void anyEndpoint_shouldReturnOk_whenAuthenticatedAsStation() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asStation("/any/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void anyEndpoint_shouldReturnOk_whenAuthenticatedAsAdmin() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asAdmin("/any/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void anyEndpoint_shouldReturnOk_whenAuthenticatedAsService() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asService("/any/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void anyEndpoint_shouldReturnUnauthorized_whenUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/any/probe"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void csrfDisabled_shouldAllowPostWithoutCsrfToken_whenAuthorized() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/probe")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN));

        // then
        result.andExpect(status().isOk());
    }
}