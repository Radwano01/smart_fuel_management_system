package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.controller.TransactionAdminController;
import com.example.smart_fuel_management_system.controller.TransactionController;
import com.example.smart_fuel_management_system.controller.TransactionInternalController;
import com.example.smart_fuel_management_system.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        TransactionInternalController.class,
        TransactionAdminController.class,
        TransactionController.class
})
@Import({SecurityConfig.class, JWTAuthenticationFilter.class, JWTAuthEntryPoint.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TransactionService transactionService;

    @Test
    void authenticationManager_isAvailable() {
        // given
        // when
        AuthenticationManager result = authenticationManager;

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void internalEndpoint_acceptsServiceAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/dashboard/summary")
                        .with(user("svc").authorities(new SimpleGrantedAuthority("SERVICE"))));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void internalEndpoint_rejectsAdminAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/dashboard/summary")
                        .with(user("adm").authorities(new SimpleGrantedAuthority("ADMIN"))));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_rejectsUserAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/dashboard/summary")
                        .with(user("u").authorities(new SimpleGrantedAuthority("USER"))));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void internalEndpoint_rejectsUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/dashboard/summary"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_acceptsAdminAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions")
                        .with(user("adm").authorities(new SimpleGrantedAuthority("ADMIN"))));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_rejectsServiceAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions")
                        .with(user("svc").authorities(new SimpleGrantedAuthority("SERVICE"))));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_rejectsUserAuthority() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions")
                        .with(user("u").authorities(new SimpleGrantedAuthority("USER"))));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_rejectsUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void transactionsEndpoint_acceptsAuthenticatedUser() throws Exception {
        // given
        String principalName = UUID.randomUUID().toString();

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/recent")
                        .with(user(principalName)
                                .authorities(new SimpleGrantedAuthority("USER"))));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void transactionsEndpoint_acceptsAdminAuthority() throws Exception {
        // given
        String principalName = UUID.randomUUID().toString();

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/recent")
                        .with(user(principalName)
                                .authorities(new SimpleGrantedAuthority("ADMIN"))));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    void transactionsEndpoint_rejectsUnauthenticated() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/recent"));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void csrfIsDisabled_postWithoutCsrfTokenSucceeds() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/transactions/stations/by-ids")
                        .contentType("application/json")
                        .content("[]")
                        .with(user("svc").authorities(new SimpleGrantedAuthority("SERVICE"))));

        // then
        result.andExpect(status().isOk());
    }
}