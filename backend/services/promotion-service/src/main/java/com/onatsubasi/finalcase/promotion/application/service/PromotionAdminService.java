package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreatePromotionRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.UpdatePromotionRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionResponse;
import com.onatsubasi.finalcase.promotion.application.port.PromotionEventPublisher;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountStrategyFactory;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import com.onatsubasi.finalcase.promotion.infrastructure.mapper.PromotionMapper;
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
public class PromotionAdminService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final DiscountStrategyFactory strategyFactory;
    private final PromotionEventPublisher eventPublisher;

    @Transactional
    public PromotionResponse create(CreatePromotionRequest request) {
        try {
            MDC.put("eventName", "promotion.create.started");
            log.info("Promotion creation started, name={}, type={}", request.name(), request.type());

            Promotion promotion = Promotion.create(
                    request.name(),
                    request.description(),
                    request.type(),
                    request.couponRequired(),
                    request.stackable(),
                    request.priority(),
                    request.ruleConfig(),
                    request.globalUsageLimit(),
                    request.perUserUsageLimit(),
                    request.startsAt(),
                    request.endsAt()
            );

            strategyFactory.getStrategy(promotion.getType()).validateConfig(promotion);

            Promotion saved = promotionRepository.save(promotion);
            eventPublisher.publishPromotionCreated(saved);

            MDC.put("eventName", "promotion.created");
            log.info("Promotion created, promotionId={}, type={}", saved.getId(), saved.getType());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public PromotionResponse update(UUID promotionId, UpdatePromotionRequest request) {
        try {
            MDC.put("eventName", "promotion.update.started");
            log.info("Promotion update started, promotionId={}", promotionId);

            Promotion promotion = getPromotionOrThrow(promotionId);

            promotion.update(
                    request.name(),
                    request.description(),
                    request.couponRequired(),
                    request.stackable(),
                    request.priority(),
                    request.ruleConfig(),
                    request.globalUsageLimit(),
                    request.perUserUsageLimit(),
                    request.startsAt(),
                    request.endsAt()
            );

            strategyFactory.getStrategy(promotion.getType()).validateConfig(promotion);

            Promotion saved = promotionRepository.save(promotion);
            eventPublisher.publishPromotionUpdated(saved);

            MDC.put("eventName", "promotion.updated");
            log.info("Promotion updated, promotionId={}", saved.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public PromotionResponse activate(UUID promotionId) {
        return changeStatus(promotionId, PromotionStatus.ACTIVE);
    }

    @Transactional
    public PromotionResponse pause(UUID promotionId) {
        return changeStatus(promotionId, PromotionStatus.PAUSED);
    }

    @Transactional
    public PromotionResponse expire(UUID promotionId) {
        return changeStatus(promotionId, PromotionStatus.EXPIRED);
    }

    @Transactional
    public void delete(UUID promotionId) {
        try {
            MDC.put("eventName", "promotion.delete.started");
            log.info("Promotion delete started, promotionId={}", promotionId);

            Promotion promotion = getPromotionOrThrow(promotionId);
            promotion.softDelete();

            Promotion saved = promotionRepository.save(promotion);
            eventPublisher.publishPromotionDeleted(saved);

            MDC.put("eventName", "promotion.deleted");
            log.info("Promotion deleted, promotionId={}", saved.getId());
        } catch (BaseException ex) {
            logBusinessFailure("promotion.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public PromotionResponse getById(UUID promotionId) {
        return promotionMapper.toResponse(getPromotionOrThrow(promotionId));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> list(PromotionStatus status) {
        List<Promotion> promotions = status == null
                ? promotionRepository.findAll()
                : promotionRepository.findByStatus(status);

        return promotions.stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    private PromotionResponse changeStatus(UUID promotionId, PromotionStatus targetStatus) {
        try {
            MDC.put("eventName", "promotion.status_change.started");
            log.info("Promotion status change started, promotionId={}, targetStatus={}", promotionId, targetStatus);

            Promotion promotion = getPromotionOrThrow(promotionId);

            switch (targetStatus) {
                case ACTIVE -> {
                    strategyFactory.getStrategy(promotion.getType()).validateConfig(promotion);
                    promotion.activate();
                }
                case PAUSED -> promotion.pause();
                case EXPIRED -> promotion.expire();
                default -> throw new BaseException(PromotionErrorCode.INVALID_PROMOTION_DATA);
            }

            Promotion saved = promotionRepository.save(promotion);

            if (targetStatus == PromotionStatus.ACTIVE) {
                eventPublisher.publishPromotionActivated(saved);
            } else if (targetStatus == PromotionStatus.PAUSED) {
                eventPublisher.publishPromotionPaused(saved);
            } else if (targetStatus == PromotionStatus.EXPIRED) {
                eventPublisher.publishPromotionExpired(saved);
            }

            MDC.put("eventName", "promotion.status_changed");
            log.info("Promotion status changed, promotionId={}, status={}", saved.getId(), saved.getStatus());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.status_change.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private Promotion getPromotionOrThrow(UUID promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_NOT_FOUND));
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Promotion admin operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}
