package com.onatsubasi.finalcase.search.presentation.controller;

import com.onatsubasi.finalcase.search.application.dto.query.ProductSearchCriteria;
import com.onatsubasi.finalcase.search.application.dto.response.SearchPageResponse;
import com.onatsubasi.finalcase.search.application.service.ProductSearchQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicProductSearchControllerTest {

    private ProductSearchQueryService queryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        queryService = mock(ProductSearchQueryService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublicProductSearchController(queryService))
                .build();
    }

    @Test
    void searchAliasBuildsCriteriaFromQueryParameters() throws Exception {
        when(queryService.search(any(ProductSearchCriteria.class)))
                .thenReturn(new SearchPageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/search")
                        .param("q", "phone")
                        .param("attr_color", "Black")
                        .param("attr_storage", "128GB")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ProductSearchCriteria> captor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(queryService).search(captor.capture());
        assertThat(captor.getValue().query()).isEqualTo("phone");
        assertThat(captor.getValue().attributes()).containsKeys("color", "storage");
    }
}
