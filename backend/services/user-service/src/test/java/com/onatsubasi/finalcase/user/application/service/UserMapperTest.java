package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshot;
import com.onatsubasi.finalcase.user.application.dto.response.ProductListResponse;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    @DisplayName("profile mapper exposes profile email reference without credential fields")
    void mapsProfileWithoutCredentialFields() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.createLazy(userId, "USER@example.com", "tr");
        profile.updateProfile("Oytun", "Coban", "+905551112233", null, "en", true);

        UserProfileResponse response = mapper.toResponse(profile);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.firstName()).isEqualTo("Oytun");
        assertThat(response.language()).isEqualTo("en");
    }

    @Test
    @DisplayName("address snapshot contains immutable checkout address fields")
    void mapsAddressSnapshot() {
        UserAddress address = UserAddress.create(
                UUID.randomUUID(), "Home", AddressType.BOTH, "Oytun Coban", "+905551112233",
                "Street 1", "Floor 2", "Kadikoy", "Istanbul", "Türkiye", "34710", true, true
        );

        AddressSnapshot snapshot = mapper.toSnapshot(address);

        assertThat(snapshot.recipientName()).isEqualTo("Oytun Coban");
        assertThat(snapshot.line1()).isEqualTo("Street 1");
        assertThat(snapshot.city()).isEqualTo("Istanbul");
    }

    @Test
    @DisplayName("product list mapper sorts items by insertion time")
    void mapsProductListItems() {
        ProductList list = ProductList.create(UUID.randomUUID(), "Wishlist", null, ProductListVisibility.PRIVATE);
        UUID firstProduct = UUID.randomUUID();
        list.addItem(firstProduct, "first");

        ProductListResponse response = mapper.toResponse(list);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(firstProduct);
    }
}
