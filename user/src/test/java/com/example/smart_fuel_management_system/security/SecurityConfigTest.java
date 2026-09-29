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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static final String USER_TOKEN = "user-token";
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter rateLimiter;

    private UUID userId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        lenient().when(jwtService.validateToken(USER_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(USER_TOKEN)).thenReturn("USER");
        lenient().when(jwtService.extractUserId(USER_TOKEN)).thenReturn(userId);

        lenient().when(jwtService.validateToken(ADMIN_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(ADMIN_TOKEN)).thenReturn("ADMIN");
        lenient().when(jwtService.extractSubject(ADMIN_TOKEN)).thenReturn(adminId.toString());

        lenient().when(jwtService.validateToken(SERVICE_TOKEN)).thenReturn(true);
        lenient().when(jwtService.extractRole(SERVICE_TOKEN)).thenReturn("SERVICE");
        lenient().when(jwtService.extractIssuer(SERVICE_TOKEN)).thenReturn("auth-service");
        lenient().when(jwtService.extractAudience(SERVICE_TOKEN)).thenReturn("user-service");
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

        @GetMapping("/api/v1/users/probe")
        ResponseEntity<String> users() {
            return ResponseEntity.ok("users");
        }

        @PostMapping("/api/v1/internal/probe")
        ResponseEntity<String> internalPost() {
            return ResponseEntity.ok("posted");
        }

        @PatchMapping("/api/v1/users/probe")
        ResponseEntity<String> usersPatch() {
            return ResponseEntity.ok("patched");
        }
    }

    private MockHttpServletRequestBuilder asUser(String path) {
        return get(path).header("Authorization", "Bearer " + USER_TOKEN);
    }

    private MockHttpServletRequestBuilder asAdmin(String path) {
        return get(path).header("Authorization", "Bearer " + ADMIN_TOKEN);
    }

    private RequestBuilder asService(String path) {
        return get(path).header("Authorization", "Bearer " + SERVICE_TOKEN);
    }

    // ---------------------------------------------------------------------
    // Context sanity
    // ---------------------------------------------------------------------

    @Test
    void authenticationManager_shouldBeAvailable_whenContextLoads() {
        // given
        // when
        AuthenticationManager result = authenticationManager;

        // then
        assertThat(result).isNotNull();
    }

    // ---------------------------------------------------------------------
    // /api/v1/internal/**  ->  hasAuthority("SERVICE")
    // ---------------------------------------------------------------------

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
    void internalEndpoint_shouldReturnForbidden_whenAuthorityIsUser() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asUser("/api/v1/internal/probe"));

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

    // ---------------------------------------------------------------------
    // /api/v1/admin/**  ->  hasAuthority("ADMIN")
    // ---------------------------------------------------------------------

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
    void adminEndpoint_shouldReturnForbidden_whenAuthorityIsUser() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asUser("/api/v1/admin/probe"));

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

    // ---------------------------------------------------------------------
    // /api/v1/users/**  ->  authenticated()
    // ---------------------------------------------------------------------

    @Test
    void usersEndpoint_shouldReturnOk_whenAuthenticatedAsUser() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asUser("/api/v1/users/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void usersEndpoint_shouldReturnOk_whenAuthenticatedAsAdmin() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asAdmin("/api/v1/users/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void usersEndpoint_shouldReturnOk_whenAuthenticatedAsService() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(asService("/api/v1/users/probe"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void usersEndpoint_shouldReturnUnauthorized_whenUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/users/probe"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void csrfDisabled_shouldAllowPostWithoutCsrfToken_whenAuthorized() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/probe")
                        .header("Authorization", "Bearer " + SERVICE_TOKEN));

        // then
        result.andExpect(status().isOk());
    }
}