package com.onatsubasi.finalcase.review.testsupport;

import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ReviewTestData {

    public static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID OTHER_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ADMIN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID ORDER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID ORDER_ITEM_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID REVIEW_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    private ReviewTestData() {
    }

    public static UserContext customer() {
        return new UserContext(USER_ID, "customer@example.com", Set.of("CUSTOMER"));
    }

    public static UserContext admin() {
        return new UserContext(ADMIN_ID, "admin@example.com", Set.of("ADMIN"));
    }

    public static Review approvedReview() {
        Review review = Review.createVerifiedPurchaseReview(
                PRODUCT_ID,
                USER_ID,
                ORDER_ID,
                ORDER_ITEM_ID,
                "ORD-20260503-000001",
                Instant.parse("2026-05-03T10:00:00Z"),
                "C***",
                5,
                "Great",
                "Fast delivery and good product",
                List.of(Map.of("url", "https://cdn.example.com/review-1.jpg", "sortOrder", 1)),
                true
        );
        ReflectionTestUtils.setField(review, "id", REVIEW_ID);
        return review;
    }

    public static Review pendingReview() {
        Review review = Review.createVerifiedPurchaseReview(
                PRODUCT_ID,
                USER_ID,
                ORDER_ID,
                ORDER_ITEM_ID,
                "ORD-20260503-000001",
                Instant.parse("2026-05-03T10:00:00Z"),
                "C***",
                4,
                "Good",
                "Works as expected",
                List.of(),
                false
        );
        ReflectionTestUtils.setField(review, "id", REVIEW_ID);
        return review;
    }
}
