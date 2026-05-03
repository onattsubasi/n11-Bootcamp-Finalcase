package com.onatsubasi.finalcase.review.infrastructure.messaging;

public final class ReviewEventTypes {

    private ReviewEventTypes() {
    }

    public static final String REVIEW_SUBMITTED = "review.submitted";
    public static final String REVIEW_APPROVED = "review.approved";
    public static final String REVIEW_REJECTED = "review.rejected";
    public static final String REVIEW_HIDDEN = "review.hidden";
    public static final String REVIEW_RESTORED = "review.restored";
    public static final String REVIEW_DELETED = "review.deleted";
    public static final String REVIEW_UPDATED = "review.updated";

    public static final String RATING_SUMMARY_UPDATED = "review.rating_summary.updated";

    public static final String REVIEW_VOTED = "review.voted";
    public static final String REVIEW_VOTE_REMOVED = "review.vote.removed";

    public static final String REVIEW_REPORTED = "review.reported";
    public static final String REVIEW_REPORT_RESOLVED = "review.report.resolved";
}