package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.AddFavoriteProductRequest;
import com.onatsubasi.finalcase.user.application.dto.response.FavoriteProductResponse;
import com.onatsubasi.finalcase.user.application.service.FavoriteProductService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerFavoriteControllerTest {

    private FavoriteProductService favoriteProductService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        favoriteProductService = mock(FavoriteProductService.class);
        userId = UUID.randomUUID();
        UserContext context = TestUserContexts.customer(userId, "user@example.com");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerFavoriteController(favoriteProductService))
                .setCustomArgumentResolvers(new FixedUserContextArgumentResolver(context))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("favorites endpoint returns product references only")
    void listFavorites() throws Exception {
        UUID productId = UUID.randomUUID();
        when(favoriteProductService.listMyFavorites(any()))
                .thenReturn(List.of(new FavoriteProductResponse(UUID.randomUUID(), userId, productId, Instant.now())));

        mockMvc.perform(get("/api/customer/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(productId.toString()));
    }

    @Test
    @DisplayName("add favorite returns 201 for newly created or existing reference")
    void addFavorite() throws Exception {
        UUID productId = UUID.randomUUID();
        when(favoriteProductService.addFavorite(any(), any()))
                .thenReturn(new FavoriteProductResponse(UUID.randomUUID(), userId, productId, Instant.now()));

        mockMvc.perform(post("/api/customer/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddFavoriteProductRequest(productId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productId").value(productId.toString()));
    }

    @Test
    @DisplayName("remove favorite is idempotent from HTTP perspective")
    void removeFavorite() throws Exception {
        mockMvc.perform(delete("/api/customer/favorites/{productId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
