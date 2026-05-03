package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSessionResponse;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutSessionRepository;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutQueryService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutMapper checkoutMapper;

    @Transactional(readOnly = true)
    public CheckoutSessionResponse getMyCheckout(UserContext currentUser, UUID checkoutId) {
        CheckoutSession session = checkoutSessionRepository.findById(checkoutId)
                .orElseThrow(() -> new BaseException(CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND));

        session.assertOwnedBy(currentUser.userId());
        return checkoutMapper.toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public CheckoutSessionResponse getAdminCheckout(UUID checkoutId) {
        CheckoutSession session = checkoutSessionRepository.findById(checkoutId)
                .orElseThrow(() -> new BaseException(CheckoutErrorCode.CHECKOUT_SESSION_NOT_FOUND));

        return checkoutMapper.toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public Page<CheckoutSessionResponse> listMyCheckouts(UserContext currentUser, Pageable pageable) {
        return checkoutSessionRepository.findByUserId(currentUser.userId(), pageable)
                .map(checkoutMapper::toSessionResponse);
    }

    @Transactional(readOnly = true)
    public Page<CheckoutSessionResponse> listAdminCheckouts(
            com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus status,
            Pageable pageable
    ) {
        if (status != null) {
            return checkoutSessionRepository.findByStatus(status, pageable)
                    .map(checkoutMapper::toSessionResponse);
        }
        return checkoutSessionRepository.findAll(pageable)
                .map(checkoutMapper::toSessionResponse);
    }
}
