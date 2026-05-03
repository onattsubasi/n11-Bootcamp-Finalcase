package com.onatsubasi.finalcase.user.infrastructure.mapper;

import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshot;
import com.onatsubasi.finalcase.user.application.dto.response.*;
import com.onatsubasi.finalcase.user.domain.entity.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUserId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getAvatarUrl(),
                profile.getLanguage(),
                profile.isMarketingOptIn(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public UserAddressResponse toResponse(UserAddress address) {
        return new UserAddressResponse(
                address.getId(),
                address.getUserId(),
                address.getTitle(),
                address.getType(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getLine1(),
                address.getLine2(),
                address.getDistrict(),
                address.getCity(),
                address.getCountry(),
                address.getPostalCode(),
                address.isDefaultShipping(),
                address.isDefaultBilling(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    public UserPreferenceResponse toResponse(UserPreference preference) {
        return new UserPreferenceResponse(
                preference.getId(),
                preference.getUserId(),
                preference.getLanguage(),
                preference.getCurrency(),
                preference.isMarketingEmailEnabled(),
                preference.isNotificationEmailEnabled(),
                preference.isNotificationInAppEnabled(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }

    public FavoriteProductResponse toResponse(FavoriteProduct favoriteProduct) {
        return new FavoriteProductResponse(
                favoriteProduct.getId(),
                favoriteProduct.getUserId(),
                favoriteProduct.getProductId(),
                favoriteProduct.getCreatedAt()
        );
    }

    public ProductListResponse toResponse(ProductList productList) {
        List<ProductListItemResponse> items = productList.getItems()
                .stream()
                .sorted(Comparator.comparing(ProductListItem::getCreatedAt))
                .map(this::toResponse)
                .toList();

        return new ProductListResponse(
                productList.getId(),
                productList.getUserId(),
                productList.getName(),
                productList.getDescription(),
                productList.getVisibility(),
                items,
                productList.getCreatedAt(),
                productList.getUpdatedAt()
        );
    }

    public ProductListItemResponse toResponse(ProductListItem item) {
        return new ProductListItemResponse(
                item.getId(),
                item.getProductId(),
                item.getNote(),
                item.getCreatedAt()
        );
    }

    public AddressSnapshot toSnapshot(UserAddress address) {
        return new AddressSnapshot(
                address.getId(),
                address.getType(),
                address.getTitle(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getLine1(),
                address.getLine2(),
                address.getDistrict(),
                address.getCity(),
                address.getCountry(),
                address.getPostalCode()
        );
    }
}