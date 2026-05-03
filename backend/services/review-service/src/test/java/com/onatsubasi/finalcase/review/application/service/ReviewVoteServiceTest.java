package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.application.dto.request.VoteReviewRequest;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.enums.ReviewVoteType;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.domain.repository.ReviewVoteRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewVoteServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ReviewVoteRepository voteRepository;

    @Mock
    ReviewEventPublisher eventPublisher;

    @Test
    void createsVoteAndUpdatesCounters() {
        Review review = ReviewTestData.approvedReview();
        ReviewVoteService service = new ReviewVoteService(
                reviewRepository,
                voteRepository,
                eventPublisher,
                new ReviewMapper()
        );

        when(reviewRepository.findByIdForUpdate(ReviewTestData.REVIEW_ID)).thenReturn(Optional.of(review));
        when(voteRepository.findByReviewIdAndUserId(ReviewTestData.REVIEW_ID, ReviewTestData.OTHER_USER_ID))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(ReviewVote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.vote(
                new com.onatsubasi.finalcase.common.security.UserContext(
                        ReviewTestData.OTHER_USER_ID,
                        "other@example.com",
                        java.util.Set.of("CUSTOMER")
                ),
                ReviewTestData.REVIEW_ID,
                new VoteReviewRequest(ReviewVoteType.HELPFUL)
        );

        assertThat(review.getHelpfulCount()).isEqualTo(1);
        verify(eventPublisher).publishReviewVoted(any(ReviewVote.class));
    }

    @Test
    void rejectsSelfVoteAtDomainBoundary() {
        Review review = ReviewTestData.approvedReview();
        ReviewVoteService service = new ReviewVoteService(
                reviewRepository,
                voteRepository,
                eventPublisher,
                new ReviewMapper()
        );

        when(reviewRepository.findByIdForUpdate(ReviewTestData.REVIEW_ID)).thenReturn(Optional.of(review));
        when(voteRepository.findByReviewIdAndUserId(ReviewTestData.REVIEW_ID, ReviewTestData.USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vote(
                ReviewTestData.customer(),
                ReviewTestData.REVIEW_ID,
                new VoteReviewRequest(ReviewVoteType.HELPFUL)
        ))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_SELF_VOTE_NOT_ALLOWED);
    }
}
