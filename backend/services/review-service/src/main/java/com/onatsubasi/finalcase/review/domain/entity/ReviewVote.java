package com.onatsubasi.finalcase.review.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.domain.enums.ReviewVoteType;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "review_votes",
        indexes = {
                @Index(name = "idx_review_votes_review_id", columnList = "review_id"),
                @Index(name = "idx_review_votes_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_votes_review_user",
                        columnNames = {"review_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 30)
    private ReviewVoteType voteType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private ReviewVote(
            Review review,
            UUID userId,
            ReviewVoteType voteType
    ) {
        validateReview(review);
        validateUserId(userId);
        validateVoteType(voteType);

        if (review.getUserId().equals(userId)) {
            throw new BaseException(ReviewErrorCode.REVIEW_SELF_VOTE_NOT_ALLOWED);
        }

        this.review = review;
        this.userId = userId;
        this.voteType = voteType;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        applyIncrement(voteType);
    }

    public static ReviewVote create(
            Review review,
            UUID userId,
            ReviewVoteType voteType
    ) {
        return new ReviewVote(review, userId, voteType);
    }

    public boolean changeVote(ReviewVoteType newVoteType) {
        validateVoteType(newVoteType);

        if (newVoteType == this.voteType) {
            return false;
        }

        applyDecrement(this.voteType);
        this.voteType = newVoteType;
        applyIncrement(newVoteType);
        touch();

        return true;
    }

    public void removeVoteEffect() {
        applyDecrement(this.voteType);
        touch();
    }

    private void applyIncrement(ReviewVoteType type) {
        if (type == ReviewVoteType.HELPFUL) {
            review.incrementHelpful();
        } else {
            review.incrementUnhelpful();
        }
    }

    private void applyDecrement(ReviewVoteType type) {
        if (type == ReviewVoteType.HELPFUL) {
            review.decrementHelpful();
        } else {
            review.decrementUnhelpful();
        }
    }

    private void validateReview(Review review) {
        if (review == null) {
            throw new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        if (review.isDeleted()) {
            throw new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }
    }

    private void validateVoteType(ReviewVoteType voteType) {
        if (voteType == null) {
            throw new BaseException(ReviewErrorCode.REVIEW_INVALID_DATA, "Vote type is required");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}