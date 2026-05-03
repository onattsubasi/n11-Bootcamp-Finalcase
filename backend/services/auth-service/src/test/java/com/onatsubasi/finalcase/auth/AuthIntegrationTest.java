package com.onatsubasi.finalcase.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.auth.application.dto.request.ChangePasswordRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.LoginRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.RegisterRequest;
import com.onatsubasi.finalcase.auth.domain.repository.AuthUserRepository;
import com.onatsubasi.finalcase.auth.domain.repository.RefreshTokenRepository;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AuthUserRepository authUserRepository;

        @Autowired
        private RefreshTokenRepository refreshTokenRepository;


        @AfterEach
        void tearDown() {
                refreshTokenRepository.deleteAll();
                authUserRepository.deleteAll();
        }

        @Test
        @DisplayName("Should register and then login successfully")
        void shouldRegisterAndLogin() throws Exception {
                RegisterRequest registerRequest = new RegisterRequest("integration@example.com", "password123");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").isString())
                                .andExpect(header().exists("Set-Cookie"));

                assertThat(authUserRepository.existsByEmail("integration@example.com")).isTrue();

                LoginRequest loginRequest = new LoginRequest("integration@example.com", "password123");

                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").isString())
                                .andExpect(header().exists("Set-Cookie"))
                                .andReturn();

                String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
                assertThat(setCookie).contains("refresh_token");
        }

        @Test
        @DisplayName("Should refresh token and rotate cookie")
        void shouldRefreshToken() throws Exception {
                RegisterRequest registerRequest = new RegisterRequest("refresh@example.com", "password123");

                MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isOk())
                        .andExpect(header().exists("Set-Cookie"))
                        .andReturn();

                Cookie refreshCookie = registerResult.getResponse().getCookie("refresh_token");

                assertThat(refreshCookie).isNotNull();
                assertThat(refreshCookie.getValue()).isNotBlank();

                mockMvc.perform(post("/api/auth/refresh")
                                .cookie(new Cookie("refresh_token", refreshCookie.getValue())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.accessToken").isString())
                        .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("Should return account summary from gateway headers")
        void shouldReturnMeFromGatewayHeaders() throws Exception {
                AuthUser user = authUserRepository
                                .saveAndFlush(new AuthUser("me@example.com", "hash", java.util.Set.of("CUSTOMER")));

                mockMvc.perform(get("/api/auth/me")
                                .header(PlatformHeaders.X_USER_ID, user.getId().toString())
                                .header(PlatformHeaders.X_USER_EMAIL, user.getEmail())
                                .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.userId").value(user.getId().toString()))
                                .andExpect(jsonPath("$.data.email").value("me@example.com"));
        }

        @Test
        @DisplayName("Should change password and clear refresh cookie")
        void shouldChangePassword() throws Exception {
                RegisterRequest registerRequest = new RegisterRequest("change@example.com", "password123");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                AuthUser user = authUserRepository.findByEmail("change@example.com").orElseThrow();
                ChangePasswordRequest request = new ChangePasswordRequest("password123", "password456");

                mockMvc.perform(post("/api/auth/change-password")
                                .header(PlatformHeaders.X_USER_ID, user.getId().toString())
                                .header(PlatformHeaders.X_USER_EMAIL, user.getEmail())
                                .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNoContent())
                                .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("Should return 401 for invalid login")
        void shouldReturn401ForInvalidLogin() throws Exception {
                LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "wrong-password");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }
}
