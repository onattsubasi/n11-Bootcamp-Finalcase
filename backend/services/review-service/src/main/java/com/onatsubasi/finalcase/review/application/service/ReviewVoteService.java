package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.VoteReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.response.ReviewVoteResponse;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.domain.repository.ReviewVoteRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewVoteService {

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository voteRepository;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewVoteResponse vote(
            UserContext currentUser,
            UUID reviewId,
            VoteReviewRequest request
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.vote.started");
            MDC.put("userId", userId.toString());

            Review review = reviewRepository.findByIdForUpdate(reviewId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

            ReviewVote vote = voteRepository.findByReviewIdAndUserId(reviewId, userId)
                    .orElse(null);

            if (vote == null) {
                vote = ReviewVote.create(review, userId, request.voteType());
            } else {
                vote.changeVote(request.voteType());
            }

            ReviewVote saved = voteRepository.save(vote);
            reviewRepository.save(review);

            eventPublisher.publishReviewVoted(saved);

            MDC.put("eventName", "review.voted");
            log.info("Review vote saved, reviewId={}, userId={}, voteType={}",
                    reviewId,
                    userId,
                    request.voteType());

            return reviewMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("review.vote.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void removeVote(
            UserContext currentUser,
            UUID reviewId
    ) {
        try {
            UUID userId = requireUserId(currentUser);

            MDC.put("eventName", "review.vote.remove.started");
            MDC.put("userId", userId.toString());

            ReviewVote vote = voteRepository.findByReviewIdAndUserId(reviewId, userId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_VOTE_NOT_FOUND));

            Review review = reviewRepository.findByIdForUpdate(reviewId)
                    .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

            vote.removeVoteEffect();
            voteRepository.delete(vote);
            reviewRepository.save(review);

            eventPublisher.publishReviewVoteRemoved(review);

            MDC.put("eventName", "review.vote.removed");
            log.info("Review vote removed, reviewId={}, userId={}", reviewId, userId);
        } catch (BaseException ex) {
            logBusinessFailure("review.vote.remove.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private UUID requireUserId(UserContext currentUser) {
        if (currentUser == null || !currentUser.isAuthenticated() || currentUser.userId() == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }

        return currentUser.userId();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Review vote operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}