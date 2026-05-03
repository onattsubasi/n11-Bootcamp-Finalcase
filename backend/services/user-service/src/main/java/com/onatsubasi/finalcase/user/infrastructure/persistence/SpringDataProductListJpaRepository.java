package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductListJpaRepository extends JpaRepository<ProductList, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<ProductList> findByIdAndUserIdAndDeletedFalse(UUID listId, UUID userId);

    @EntityGraph(attributePaths = "items")
    List<ProductList> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);
}
