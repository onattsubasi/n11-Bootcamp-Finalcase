package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.CreateAddressRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserAddressResponse;
import com.onatsubasi.finalcase.user.application.service.UserAddressService;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerAddressControllerTest {

    private UserAddressService userAddressService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userAddressService = mock(UserAddressService.class);
        userId = UUID.randomUUID();
        UserContext context = TestUserContexts.customer(userId, "user@example.com");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerAddressController(userAddressService))
                .setCustomArgumentResolvers(new FixedUserContextArgumentResolver(context))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/customer/addresses lists only current customer's addresses")
    void listMyAddresses() throws Exception {
        UUID addressId = UUID.randomUUID();
        when(userAddressService.listMyAddresses(any())).thenReturn(List.of(address(addressId, true, false)));

        mockMvc.perform(get("/api/customer/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(addressId.toString()))
                .andExpect(jsonPath("$.data[0].defaultShipping").value(true));
    }

    @Test
    @DisplayName("POST /api/customer/addresses creates address and returns 201")
    void createAddress() throws Exception {
        UUID addressId = UUID.randomUUID();
        CreateAddressRequest request = new CreateAddressRequest(
                "Home", AddressType.BOTH, "Oytun Coban", "+905551112233", "Street 1", null,
                "Kadikoy", "Istanbul", "Türkiye", "34710", true, true
        );
        when(userAddressService.createAddress(any(), any())).thenReturn(address(addressId, true, true));

        mockMvc.perform(post("/api/customer/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(addressId.toString()))
                .andExpect(jsonPath("$.data.defaultBilling").value(true));
    }

    private UserAddressResponse address(UUID addressId, boolean defaultShipping, boolean defaultBilling) {
        return new UserAddressResponse(
                addressId,
                userId,
                "Home",
                AddressType.BOTH,
                "Oytun Coban",
                "+905551112233",
                "Street 1",
                null,
                "Kadikoy",
                "Istanbul",
                "Türkiye",
                "34710",
                defaultShipping,
                defaultBilling,
                Instant.now(),
                Instant.now()
        );
    }
}
