package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserPreferenceRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserPreferenceResponse;
import com.onatsubasi.finalcase.user.application.service.UserPreferenceService;
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

class CustomerPreferenceControllerTest {

    private UserPreferenceService userPreferenceService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userPreferenceService = mock(UserPreferenceService.class);
        userId = UUID.randomUUID();
        UserContext context = TestUserContexts.customer(userId, "user@example.com");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerPreferenceController(userPreferenceService))
                .setCustomArgumentResolvers(new FixedUserContextArgumentResolver(context))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/customer/preferences/me returns current user's preference defaults")
    void getPreferences() throws Exception {
        when(userPreferenceService.getMyPreferences(any())).thenReturn(preferences("tr", "TRY"));

        mockMvc.perform(get("/api/customer/preferences/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("tr"))
                .andExpect(jsonPath("$.data.currency").value("TRY"));
    }

    @Test
    @DisplayName("PUT /api/customer/preferences/me updates notification preferences")
    void updatePreferences() throws Exception {
        when(userPreferenceService.updateMyPreferences(any(), any())).thenReturn(preferences("en", "USD"));

        mockMvc.perform(put("/api/customer/preferences/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserPreferenceRequest(
                                "en", "USD", true, false, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("en"))
                .andExpect(jsonPath("$.data.notificationEmailEnabled").value(false));
    }

    private UserPreferenceResponse preferences(String language, String currency) {
        return new UserPreferenceResponse(
                UUID.randomUUID(),
                userId,
                language,
                currency,
                true,
                false,
                true,
                Instant.now(),
                Instant.now()
        );
    }
}
