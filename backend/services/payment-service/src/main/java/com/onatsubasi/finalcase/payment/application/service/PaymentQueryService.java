package com.onatsubasi.finalcase.payment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.payment.domain.exception.PaymentErrorCode;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentRefundResponse;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentSummaryResponse;
import com.onatsubasi.finalcase.payment.infrastructure.mapper.PaymentMapper;
import com.onatsubasi.finalcase.payment.domain.entity.Payment;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRefundRepository;
import com.onatsubasi.finalcase.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService {

        private final PaymentRepository paymentRepository;
        private final PaymentRefundRepository refundRepository;
        private final PaymentMapper paymentMapper;

        @Transactional(readOnly = true)
        public PaymentDetailResponse getByIdForAdmin(UUID paymentId) {
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

                return paymentMapper.toDetailResponse(payment);
        }

        @Transactional(readOnly = true)
        public PaymentDetailResponse getByIdForCustomer(
                        UUID paymentId,
                        UUID userId) {
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

                if (!payment.getUserId().equals(userId)) {
                        throw new BaseException(
                                        PaymentErrorCode.INVALID_PAYMENT_DATA,
                                        "Payment access denied");
                }

                return paymentMapper.toDetailResponse(payment);
        }

        @Transactional(readOnly = true)
        public PaymentDetailResponse getByOrderIdForInternal(UUID orderId) {
                Payment payment = paymentRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

                return paymentMapper.toDetailResponse(payment);
        }

        @Transactional(readOnly = true)
        public PaymentDetailResponse getByCheckoutIdForInternal(UUID checkoutId) {
                Payment payment = paymentRepository.findByCheckoutId(checkoutId)
                                .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND));

                return paymentMapper.toDetailResponse(payment);
        }

        @Transactional(readOnly = true)
        public Page<PaymentSummaryResponse> getMyPayments(
                        UUID userId,
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(
                                Math.max(page, 0),
                                Math.min(Math.max(size, 1), 100),
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                return paymentRepository.findByUserId(userId, pageable)
                                .map(paymentMapper::toSummaryResponse);
        }

        @Transactional(readOnly = true)
        public Page<PaymentSummaryResponse> getAllPayments(
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(
                                Math.max(page, 0),
                                Math.min(Math.max(size, 1), 100),
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                return paymentRepository.findAll(pageable)
                                .map(paymentMapper::toSummaryResponse);
        }

        @Transactional(readOnly = true)
        public Page<PaymentRefundResponse> getRefunds(
                        UUID paymentId,
                        int page,
                        int size) {
                Pageable pageable = PageRequest.of(
                                Math.max(page, 0),
                                Math.min(Math.max(size, 1), 100),
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                return refundRepository.findByPaymentId(paymentId, pageable)
                                .map(paymentMapper::toRefundResponse);
        }
}