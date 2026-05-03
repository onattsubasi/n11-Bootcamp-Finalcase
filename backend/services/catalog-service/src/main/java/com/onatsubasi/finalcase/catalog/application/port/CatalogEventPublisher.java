package com.onatsubasi.finalcase.catalog.application.port;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;

public interface CatalogEventPublisher {

    void publishProductCreated(Product product);

    void publishProductUpdated(Product product);

    void publishProductPriceChanged(Product product);

    void publishProductStatusChanged(Product product);

    void publishProductDeleted(Product product);

    void publishCategoryCreated(Category category);

    void publishCategoryUpdated(Category category);

    void publishCategoryStatusChanged(Category category);

    void publishBrandCreated(Brand brand);

    void publishBrandUpdated(Brand brand);

    void publishBrandStatusChanged(Brand brand);
}