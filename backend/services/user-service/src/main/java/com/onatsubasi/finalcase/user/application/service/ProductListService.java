package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.AddProductListItemRequest;
import com.onatsubasi.finalcase.user.application.dto.request.CreateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateProductListRequest;
import com.onatsubasi.finalcase.user.application.dto.response.ProductListResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.repository.ProductListRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductListService {

    private final ProductListRepository productListRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ProductListResponse> listMyProductLists(UserContext userContext) {
        UUID userId = requireUserId(userContext);

        return productListRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductListResponse getMyProductList(UserContext userContext, UUID listId) {
        UUID userId = requireUserId(userContext);

        ProductList productList = getOwnedList(userId, listId);

        return userMapper.toResponse(productList);
    }

    @Transactional
    public ProductListResponse createProductList(
            UserContext userContext,
            CreateProductListRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.product_list.create.started");
            MDC.put("userId", userId.toString());

            ProductList productList = ProductList.create(
                    userId,
                    request.name(),
                    request.description(),
                    request.visibility()
            );

            ProductList saved = productListRepository.save(productList);
            eventPublisher.publishProductListCreated(saved);

            MDC.put("eventName", "user.product_list.created");
            log.info("Product list created, userId={}, listId={}", userId, saved.getId());

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.product_list.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductListResponse updateProductList(
            UserContext userContext,
            UUID listId,
            UpdateProductListRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            ProductList productList = getOwnedList(userId, listId);

            productList.update(
                    request.name(),
                    request.description(),
                    request.visibility()
            );

            ProductList saved = productListRepository.save(productList);
            eventPublisher.publishProductListUpdated(saved);

            log.info("Product list updated, userId={}, listId={}", userId, saved.getId());

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.product_list.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void deleteProductList(UserContext userContext, UUID listId) {
        try {
            UUID userId = requireUserId(userContext);

            ProductList productList = getOwnedList(userId, listId);
            productList.softDelete();

            ProductList saved = productListRepository.save(productList);
            eventPublisher.publishProductListDeleted(saved);

            log.info("Product list deleted, userId={}, listId={}", userId, saved.getId());
        } catch (BaseException ex) {
            logBusinessFailure("user.product_list.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductListResponse addItem(
            UserContext userContext,
            UUID listId,
            AddProductListItemRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            ProductList productList = getOwnedList(userId, listId);
            boolean created = productList.addItem(request.productId(), request.note());

            ProductList saved = productListRepository.save(productList);

            if (created) {
                eventPublisher.publishProductListItemAdded(saved, request.productId());
                MDC.put("eventName", "user.product_list.item_added");
                log.info("Product list item added, userId={}, listId={}, productId={}",
                        userId,
                        saved.getId(),
                        request.productId());
            } else {
                eventPublisher.publishProductListUpdated(saved);
                MDC.put("eventName", "user.product_list.item_note_updated");
                log.info("Product list item note updated, userId={}, listId={}, productId={}",
                        userId,
                        saved.getId(),
                        request.productId());
            }

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.product_list.item_add.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductListResponse removeItem(
            UserContext userContext,
            UUID listId,
            UUID productId
    ) {
        try {
            UUID userId = requireUserId(userContext);

            ProductList productList = getOwnedList(userId, listId);
            boolean removed = productList.removeItem(productId);

            ProductList saved = productListRepository.save(productList);

            if (removed) {
                eventPublisher.publishProductListItemRemoved(saved, productId);
                log.info("Product list item removed, userId={}, listId={}, productId={}",
                        userId,
                        saved.getId(),
                        productId);
            } else {
                log.info("Product list item remove was a no-op, userId={}, listId={}, productId={}",
                        userId,
                        saved.getId(),
                        productId);
            }

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.product_list.item_remove.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private ProductList getOwnedList(UUID userId, UUID listId) {
        return productListRepository.findByIdAndUserIdAndDeletedFalse(listId, userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.PRODUCT_LIST_NOT_FOUND));
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
        log.warn("Product list operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}
