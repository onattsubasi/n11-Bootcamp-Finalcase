package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserProfileRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.support.FixedUserContextArgumentResolver;
import com.onatsubasi.finalcase.user.support.TestUserContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerProfileControllerTest {

    private UserProfileService userProfileService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userProfileService = mock(UserProfileService.class);
        userId = UUID.randomUUID();
        UserContext context = TestUserContexts.customer(userId, "user@example.com");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerProfileController(userProfileService))
                .setCustomArgumentResolvers(new FixedUserContextArgumentResolver(context))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/customer/profile/me returns lazy-created/current profile")
    void getMyProfile() throws Exception {
        when(userProfileService.getOrCreateMyProfile(any())).thenReturn(profile("Oytun", "Coban", "tr"));

        mockMvc.perform(get("/api/customer/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Oytun"));
    }

    @Test
    @DisplayName("PUT /api/customer/profile/me updates profile-owned fields")
    void updateMyProfile() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("Oytun", "Coban", null, null, "en", true);
        when(userProfileService.updateMyProfile(any(), any())).thenReturn(profile("Oytun", "Coban", "en"));

        mockMvc.perform(put("/api/customer/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Oytun"))
                .andExpect(jsonPath("$.data.language").value("en"));
    }

    private UserProfileResponse profile(String firstName, String lastName, String language) {
        return new UserProfileResponse(
                userId,
                "user@example.com",
                firstName,
                lastName,
                null,
                null,
                language,
                true,
                UserProfileStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }
}
