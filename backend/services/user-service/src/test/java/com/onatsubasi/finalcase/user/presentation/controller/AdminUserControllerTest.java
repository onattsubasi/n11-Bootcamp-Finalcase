package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private UserProfileService userProfileService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userProfileService = mock(UserProfileService.class);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminUserController(userProfileService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("admin can list profiles by profile status")
    void listByStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userProfileService.listByStatus(UserProfileStatus.DISABLED)).thenReturn(List.of(profile(userId, UserProfileStatus.DISABLED)));

        mockMvc.perform(get("/api/admin/users?status=DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.data[0].status").value("DISABLED"));
    }

    @Test
    @DisplayName("admin disable endpoint delegates profile lifecycle, not Auth credential state")
    void disableProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userProfileService.disableProfile(userId)).thenReturn(profile(userId, UserProfileStatus.DISABLED));

        mockMvc.perform(post("/api/admin/users/{userId}/disable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    private UserProfileResponse profile(UUID userId, UserProfileStatus status) {
        return new UserProfileResponse(
                userId, "user@example.com", null, null, null, null,
                "tr", false, status, Instant.now(), Instant.now()
        );
    }
}
