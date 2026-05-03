package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshot;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotRequest;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotResponse;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserAddressService;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalUserControllerTest {

    private UserAddressService userAddressService;
    private UserProfileService userProfileService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userAddressService = mock(UserAddressService.class);
        userProfileService = mock(UserProfileService.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InternalUserController(userAddressService, userProfileService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("internal address snapshot endpoint returns immutable checkout address data")
    void addressSnapshots() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        AddressSnapshot snapshot = new AddressSnapshot(
                addressId, AddressType.BOTH, "Home", "Oytun Coban", "+905551112233",
                "Street 1", null, "Kadikoy", "Istanbul", "Türkiye", "34710"
        );
        when(userAddressService.getAddressSnapshots(any())).thenReturn(new AddressSnapshotResponse(
                userId, snapshot, snapshot, Instant.now()
        ));

        mockMvc.perform(post("/internal/users/address-snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddressSnapshotRequest(userId, addressId, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.shippingAddress.recipientName").value("Oytun Coban"));
    }

    @Test
    @DisplayName("internal profile lookup exposes profile data, not credentials")
    void internalProfileLookup() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userProfileService.getByUserId(userId)).thenReturn(new UserProfileResponse(
                userId, "user@example.com", "Oytun", "Coban", null, null,
                "tr", false, UserProfileStatus.ACTIVE, Instant.now(), Instant.now()
        ));

        mockMvc.perform(get("/internal/users/{userId}/profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Oytun"));
    }
}
