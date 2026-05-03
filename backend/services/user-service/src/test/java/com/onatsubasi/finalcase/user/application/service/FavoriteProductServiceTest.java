package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.user.application.dto.request.AddFavoriteProductRequest;
import com.onatsubasi.finalcase.user.application.dto.response.FavoriteProductResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.repository.FavoriteProductRepository;
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
class FavoriteProductServiceTest {

    @Mock
    private FavoriteProductRepository favoriteRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    private FavoriteProductService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new FavoriteProductService(favoriteRepository, new UserMapper(), eventPublisher);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("add favorite is idempotent when product is already favorited")
    void addFavoriteReturnsExistingWithoutDuplicateEvent() {
        UUID productId = UUID.randomUUID();
        FavoriteProduct existing = FavoriteProduct.create(userId, productId);
        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existing));

        FavoriteProductResponse response = service.addFavorite(
                TestUserContexts.customer(userId, "user@example.com"),
                new AddFavoriteProductRequest(productId)
        );

        assertThat(response.productId()).isEqualTo(productId);
        verify(favoriteRepository, never()).save(any());
        verify(eventPublisher, never()).publishFavoriteAdded(any());
    }

    @Test
    @DisplayName("add favorite persists new reference and publishes user.favorite.added")
    void addFavoriteSavesNewReference() {
        UUID productId = UUID.randomUUID();
        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(FavoriteProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteProductResponse response = service.addFavorite(
                TestUserContexts.customer(userId, "user@example.com"),
                new AddFavoriteProductRequest(productId)
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.productId()).isEqualTo(productId);
        verify(eventPublisher).publishFavoriteAdded(any(FavoriteProduct.class));
    }

    @Test
    @DisplayName("remove favorite is idempotent when favorite does not exist")
    void removeMissingFavoriteIsNoOp() {
        UUID productId = UUID.randomUUID();
        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());

        service.removeFavorite(TestUserContexts.customer(userId, "user@example.com"), productId);

        verify(favoriteRepository, never()).delete(any());
        verify(eventPublisher, never()).publishFavoriteRemoved(any(), any());
    }
}
