package com.onatsubasi.finalcase.user.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.CreateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.response.ProductListResponse;
import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.application.service.ProductListService;
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

class CustomerProductListControllerTest {

    private ProductListService productListService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        productListService = mock(ProductListService.class);
        userId = UUID.randomUUID();
        UserContext context = TestUserContexts.customer(userId, "user@example.com");
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerProductListController(productListService))
                .setCustomArgumentResolvers(new FixedUserContextArgumentResolver(context))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("lists custom product lists owned by current user")
    void listProductLists() throws Exception {
        UUID listId = UUID.randomUUID();
        when(productListService.listMyProductLists(any())).thenReturn(List.of(productList(listId)));

        mockMvc.perform(get("/api/customer/product-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(listId.toString()))
                .andExpect(jsonPath("$.data[0].items").isArray());
    }

    @Test
    @DisplayName("creates custom product list")
    void createProductList() throws Exception {
        UUID listId = UUID.randomUUID();
        when(productListService.createProductList(any(), any())).thenReturn(productList(listId));

        mockMvc.perform(post("/api/customer/product-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateProductListRequest(
                                "Wishlist", "For later", ProductListVisibility.PRIVATE))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(listId.toString()))
                .andExpect(jsonPath("$.data.name").value("Wishlist"));
    }

    private ProductListResponse productList(UUID listId) {
        return new ProductListResponse(
                listId,
                userId,
                "Wishlist",
                "For later",
                ProductListVisibility.PRIVATE,
                List.of(),
                Instant.now(),
                Instant.now()
        );
    }
}
