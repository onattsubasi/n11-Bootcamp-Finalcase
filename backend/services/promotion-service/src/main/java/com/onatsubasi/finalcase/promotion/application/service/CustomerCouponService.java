package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponAssignmentResponse;
import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.infrastructure.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerCouponService {

    private final CouponAssignmentRepository assignmentRepository;
    private final PromotionMapper promotionMapper;

    @Transactional(readOnly = true)
    public List<CouponAssignmentResponse> listAssignedCoupons(UserContext userContext) {
        UUID userId = requireUserId(userContext);

        return assignmentRepository.findByUserIdAndStatus(userId, CouponAssignmentStatus.ASSIGNED)
                .stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(PromotionErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }
}