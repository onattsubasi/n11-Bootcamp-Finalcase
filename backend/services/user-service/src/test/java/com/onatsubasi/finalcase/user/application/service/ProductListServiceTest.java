package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.user.application.dto.request.AddProductListItemRequest;
import com.onatsubasi.finalcase.user.application.dto.request.CreateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.response.ProductListResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.repository.ProductListRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import com.onatsubasi.finalcase.user.support.TestUserContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductListServiceTest {

    @Mock
    private ProductListRepository productListRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    private ProductListService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new ProductListService(productListRepository, new UserMapper(), eventPublisher);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("create product list stores only product references, not product display data")
    void createProductList() {
        when(productListRepository.save(any(ProductList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductListResponse response = service.createProductList(
                TestUserContexts.customer(userId, "user@example.com"),
                new CreateProductListRequest("Wishlist", "For later", ProductListVisibility.PRIVATE)
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Wishlist");
        verify(eventPublisher).publishProductListCreated(any(ProductList.class));
    }

    @Test
    @DisplayName("add new item publishes item-added event")
    void addNewItemPublishesItemAdded() {
        UUID listId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductList list = ProductList.create(userId, "Wishlist", null, ProductListVisibility.PRIVATE);
        when(productListRepository.findByIdAndUserIdAndDeletedFalse(listId, userId)).thenReturn(Optional.of(list));
        when(productListRepository.save(any(ProductList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductListResponse response = service.addItem(
                TestUserContexts.customer(userId, "user@example.com"),
                listId,
                new AddProductListItemRequest(productId, "buy later")
        );

        assertThat(response.items()).hasSize(1);
        verify(eventPublisher).publishProductListItemAdded(list, productId);
        verify(eventPublisher, never()).publishProductListUpdated(any());
    }

    @Test
    @DisplayName("adding existing item updates note and publishes list-updated instead of duplicate item-added")
    void addExistingItemPublishesListUpdatedOnly() {
        UUID listId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductList list = ProductList.create(userId, "Wishlist", null, ProductListVisibility.PRIVATE);
        list.addItem(productId, "old");
        when(productListRepository.findByIdAndUserIdAndDeletedFalse(listId, userId)).thenReturn(Optional.of(list));
        when(productListRepository.save(any(ProductList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductListResponse response = service.addItem(
                TestUserContexts.customer(userId, "user@example.com"),
                listId,
                new AddProductListItemRequest(productId, "new")
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).note()).isEqualTo("new");
        verify(eventPublisher).publishProductListUpdated(list);
        verify(eventPublisher, never()).publishProductListItemAdded(any(), any());
    }

    @Test
    @DisplayName("remove missing item is no-op and does not publish removed event")
    void removeMissingItemDoesNotPublishEvent() {
        UUID listId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductList list = ProductList.create(userId, "Wishlist", null, ProductListVisibility.PRIVATE);
        when(productListRepository.findByIdAndUserIdAndDeletedFalse(listId, userId)).thenReturn(Optional.of(list));
        when(productListRepository.save(any(ProductList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.removeItem(TestUserContexts.customer(userId, "user@example.com"), listId, productId);

        verify(eventPublisher, never()).publishProductListItemRemoved(any(), any());
    }
}
