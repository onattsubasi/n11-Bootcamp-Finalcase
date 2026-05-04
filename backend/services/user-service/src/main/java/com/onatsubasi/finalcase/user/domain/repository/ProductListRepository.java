package com.onatsubasi.finalcase.user.domain.repository;

import com.onatsubasi.finalcase.user.domain.entity.ProductList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductListRepository {

    ProductList save(ProductList productList);

    Optional<ProductList> findById(UUID listId);

    Optional<ProductList> findByIdAndUserIdAndDeletedFalse(UUID listId, UUID userId);

    List<ProductList> findByUserIdAndDeletedFalse(UUID userId);
}
