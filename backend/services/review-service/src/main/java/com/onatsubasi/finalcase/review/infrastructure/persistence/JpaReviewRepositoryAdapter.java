package com.onatsubasi.finalcase.review.infrastructure.persistence;

import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.repository.RatingSummaryStats;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaReviewRepositoryAdapter implements ReviewRepository {

    private final SpringDataReviewJpaRepository springDataRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Review save(Review review) {
        return springDataRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return springDataRepository.findById(reviewId);
    }

    @Override
    public Optional<Review> findByIdForUpdate(UUID reviewId) {
        return springDataRepository.findByIdForUpdate(reviewId);
    }

    @Override
    public Optional<Review> findByIdAndUserId(UUID reviewId, UUID userId) {
        return springDataRepository.findByIdAndUserId(reviewId, userId);
    }

    @Override
    public Optional<Review> findActiveByUserIdAndProductId(UUID userId, UUID productId) {
        return springDataRepository.findActiveByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean existsActiveByUserIdAndProductId(UUID userId, UUID productId) {
        return springDataRepository.existsActiveByUserIdAndProductId(userId, productId);
    }

    @Override
    public Page<Review> findPublicReviews(
            UUID productId,
            Integer rating,
            boolean withImagesOnly,
            Pageable pageable
    ) {
        StringBuilder where = new StringBuilder("""
                where product_id = :productId
                  and status = 'APPROVED'
                  and visible = true
                  and deleted_at is null
                """);

        if (rating != null) {
            where.append(" and rating = :rating ");
        }

        if (withImagesOnly) {
            where.append(" and jsonb_array_length(coalesce(images, '[]'::jsonb)) > 0 ");
        }

        String orderBy = orderBy(pageable.getSort());

        String selectSql = """
                select *
                  from reviews
                """ + where + orderBy;

        String countSql = """
                select count(*)
                  from reviews
                """ + where;

        Query selectQuery = entityManager.createNativeQuery(selectSql, Review.class);
        Query countQuery = entityManager.createNativeQuery(countSql);

        bindPublicReviewParameters(selectQuery, productId, rating);
        bindPublicReviewParameters(countQuery, productId, rating);

        selectQuery.setFirstResult((int) pageable.getOffset());
        selectQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Review> content = selectQuery.getResultList();

        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(
                content,
                pageable,
                total.longValue()
        );
    }

    @Override
    public Page<Review> findByUserId(UUID userId, Pageable pageable) {
        return springDataRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Review> findByStatus(ReviewStatus status, Pageable pageable) {
        return springDataRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Review> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public RatingSummaryStats calculateSummaryStats(UUID productId) {
        Query query = entityManager.createNativeQuery("""
                select
                    coalesce(sum(case when rating = 1 then 1 else 0 end), 0) as rating1_count,
                    coalesce(sum(case when rating = 2 then 1 else 0 end), 0) as rating2_count,
                    coalesce(sum(case when rating = 3 then 1 else 0 end), 0) as rating3_count,
                    coalesce(sum(case when rating = 4 then 1 else 0 end), 0) as rating4_count,
                    coalesce(sum(case when rating = 5 then 1 else 0 end), 0) as rating5_count
                  from reviews
                 where product_id = :productId
                   and status = 'APPROVED'
                   and visible = true
                   and deleted_at is null
                """);

        query.setParameter("productId", productId);

        Object[] row = (Object[]) query.getSingleResult();

        return new RatingSummaryStats(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4])
        );
    }

    private void bindPublicReviewParameters(
            Query query,
            UUID productId,
            Integer rating
    ) {
        query.setParameter("productId", productId);

        if (rating != null) {
            query.setParameter("rating", rating);
        }
    }

    private String orderBy(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return " order by created_at desc ";
        }

        Sort.Order order = sort.iterator().next();
        String direction = order.isAscending() ? " asc " : " desc ";

        String column = switch (order.getProperty()) {
            case "rating" -> "rating";
            case "helpfulCount" -> "helpful_count";
            case "createdAt" -> "created_at";
            default -> "created_at";
        };

        return " order by " + column + direction + ", id asc ";
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }

        return Long.parseLong(value.toString());
    }
}
