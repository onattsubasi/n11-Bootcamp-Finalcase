package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.AddFavoriteProductRequest;
import com.onatsubasi.finalcase.user.application.dto.response.FavoriteProductResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.repository.FavoriteProductRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteProductService {

    private final FavoriteProductRepository favoriteRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<FavoriteProductResponse> listMyFavorites(UserContext userContext) {
        UUID userId = requireUserId(userContext);

        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public FavoriteProductResponse addFavorite(
            UserContext userContext,
            AddFavoriteProductRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.favorite.add.started");
            MDC.put("userId", userId.toString());

            FavoriteProduct existing = favoriteRepository
                    .findByUserIdAndProductId(userId, request.productId())
                    .orElse(null);

            if (existing != null) {
                return userMapper.toResponse(existing);
            }

            FavoriteProduct favoriteProduct = FavoriteProduct.create(userId, request.productId());

            FavoriteProduct saved = favoriteRepository.save(favoriteProduct);
            eventPublisher.publishFavoriteAdded(saved);

            MDC.put("eventName", "user.favorite.added");
            log.info("Favorite product added, userId={}, productId={}", userId, request.productId());

            return userMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            FavoriteProduct existing = favoriteRepository
                    .findByUserIdAndProductId(requireUserId(userContext), request.productId())
                    .orElseThrow(() -> ex);

            return userMapper.toResponse(existing);
        } catch (BaseException ex) {
            logBusinessFailure("user.favorite.add.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void removeFavorite(UserContext userContext, UUID productId) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.favorite.remove.started");
            MDC.put("userId", userId.toString());

            favoriteRepository.findByUserIdAndProductId(userId, productId)
                    .ifPresent(favorite -> {
                        favoriteRepository.delete(favorite);
                        eventPublisher.publishFavoriteRemoved(userId, productId);
                        log.info("Favorite product removed, userId={}, productId={}", userId, productId);
                    });
        } catch (BaseException ex) {
            logBusinessFailure("user.favorite.remove.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Favorite operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}
