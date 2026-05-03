package com.onatsubasi.finalcase.user.application.port;

import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;

import java.util.UUID;

public interface UserEventPublisher {

    void publishProfileCreated(UserProfile profile);

    void publishProfileUpdated(UserProfile profile);

    void publishAddressCreated(UserAddress address);

    void publishAddressUpdated(UserAddress address);

    void publishAddressDeleted(UserAddress address);

    void publishFavoriteAdded(FavoriteProduct favoriteProduct);

    void publishFavoriteRemoved(UUID userId, UUID productId);

    void publishProductListCreated(ProductList productList);

    void publishProductListUpdated(ProductList productList);

    void publishProductListDeleted(ProductList productList);

    void publishProductListItemAdded(ProductList productList, UUID productId);

    void publishProductListItemRemoved(ProductList productList, UUID productId);
}
