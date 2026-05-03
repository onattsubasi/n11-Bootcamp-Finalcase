package com.onatsubasi.finalcase.basket.application.service;

import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.AddBasketItemRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateBasketItemQuantityRequest;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.port.BasketEventPublisher;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.exception.BasketErrorCode;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.domain.repository.BasketRepository;
import com.onatsubasi.finalcase.basket.infrastructure.mapper.BasketMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private BasketMapper basketMapper;

    @Mock
    private BasketEventPublisher eventPublisher;

    @InjectMocks
    private BasketService basketService;

    private UUID userId;
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userContext = new UserContext(userId, "customer@example.com", Set.of("CUSTOMER"));
    }

    @Test
    @DisplayName("getBasket lazily creates one active basket for a customer")
    void getBasketLazilyCreatesActiveBasket() {
        Basket createdBasket = Basket.empty(userId);
        BasketResponse response = responseFor(createdBasket);

        when(basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(basketRepository.saveAndFlush(any(Basket.class))).thenReturn(createdBasket);
        when(basketMapper.toResponse(createdBasket)).thenReturn(response);

        BasketResponse result = basketService.getBasket(userContext);

        assertThat(result.userId()).isEqualTo(userId);
        verify(basketRepository).saveAndFlush(any(Basket.class));
        verify(eventPublisher).publishBasketCreated(createdBasket);
    }

    @Test
    @DisplayName("addItem increments customer intent and publishes item added event")
    void addItemPublishesEvent() {
        UUID productId = UUID.randomUUID();
        Basket basket = Basket.empty(userId);
        AddBasketItemRequest request = new AddBasketItemRequest(productId, 2);

        when(basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(basket));
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(basketMapper.toResponse(any(Basket.class))).thenAnswer(invocation -> responseFor((Basket) invocation.getArgument(0)));

        BasketResponse result = basketService.addItem(userContext, request);

        assertThat(result.totalQuantity()).isEqualTo(2);
        verify(eventPublisher).publishItemAdded(any(Basket.class), eq(productId));
    }

    @Test
    @DisplayName("removeItem is idempotent and does not publish event when item does not exist")
    void removeItemDoesNotPublishWhenItemMissing() {
        Basket basket = Basket.empty(userId);
        UUID productId = UUID.randomUUID();

        when(basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(basket));

        basketService.removeItem(userContext, productId);

        verify(basketRepository, never()).save(any(Basket.class));
        verify(eventPublisher, never()).publishItemRemoved(any(Basket.class), eq(productId));
    }

    @Test
    @DisplayName("internal basket snapshot rejects empty basket with BASKET_EMPTY")
    void internalSnapshotRejectsEmptyBasket() {
        Basket basket = Basket.empty(userId);

        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.of(basket));

        assertThatThrownBy(() -> basketService.getBasket(userId))
                .isInstanceOfSatisfying(BaseException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(BasketErrorCode.BASKET_EMPTY));
    }

    @Test
    @DisplayName("markBasketCheckedOut saves lifecycle state and publishes checked-out event")
    void markBasketCheckedOutPublishesEvent() {
        Basket basket = Basket.empty(userId);
        basket.addItem(UUID.randomUUID(), 1);
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(basketRepository.findByIdForUpdate(basketId)).thenReturn(Optional.of(basket));
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        basketService.markBasketCheckedOut(basketId, new MarkBasketCheckedOutRequest(orderId));

        assertThat(basket.getStatus()).isEqualTo(BasketStatus.CHECKED_OUT);
        assertThat(basket.getOrderId()).isEqualTo(orderId);
        verify(eventPublisher).publishBasketCheckedOut(basket);
    }

    @Test
    @DisplayName("updateQuantity rejects missing active basket")
    void updateQuantityRejectsMissingBasket() {
        when(basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> basketService.updateItemQuantity(
                userContext,
                UUID.randomUUID(),
                new UpdateBasketItemQuantityRequest(2)
        ))
                .isInstanceOfSatisfying(BaseException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(BasketErrorCode.BASKET_NOT_FOUND));
    }

    private BasketResponse responseFor(Basket basket) {
        return new BasketResponse(
                basket.getId(),
                basket.getUserId(),
                basket.getStatus(),
                basket.getCouponCodeIntent(),
                List.of(),
                basket.itemCount(),
                basket.totalQuantity(),
                basket.isEmpty(),
                basket.getUpdatedAt() == null ? Instant.now() : basket.getUpdatedAt()
        );
    }
}
