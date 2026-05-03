package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.AbstractIntegrationTest;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SpringDataUserProfileJpaRepository profileRepository;

    @Autowired
    private SpringDataUserAddressJpaRepository addressRepository;

    @Autowired
    private SpringDataFavoriteProductJpaRepository favoriteRepository;

    @Autowired
    private SpringDataProductListJpaRepository productListRepository;

    @Test
    @DisplayName("Flyway schema supports profile lookup by same AuthAccount userId")
    void profilePersistsWithAuthAccountUserId() {
        UUID userId = UUID.randomUUID();
        profileRepository.saveAndFlush(UserProfile.createLazy(userId, "user@example.com", "tr"));

        assertThat(profileRepository.findByUserId(userId)).isPresent();
    }

    @Test
    @DisplayName("partial unique index allows only one default shipping address per active user address set")
    void onlyOneDefaultShippingAddressAllowed() {
        UUID userId = UUID.randomUUID();
        addressRepository.save(address(userId, "Home", true, false));
        addressRepository.save(address(userId, "Work", true, false));

        assertThatThrownBy(addressRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("favorite product unique constraint prevents duplicate user/product references")
    void duplicateFavoriteRejectedByDatabase() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        favoriteRepository.save(FavoriteProduct.create(userId, productId));
        favoriteRepository.save(FavoriteProduct.create(userId, productId));

        assertThatThrownBy(favoriteRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("product list item unique constraint prevents duplicate references in same list")
    void duplicateProductListItemRejectedByDomainBeforeDatabase() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductList list = ProductList.create(userId, "Wishlist", null, ProductListVisibility.PRIVATE);
        list.addItem(productId, "first");
        list.addItem(productId, "second");

        ProductList saved = productListRepository.saveAndFlush(list);

        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getNote()).isEqualTo("second");
    }

    private UserAddress address(UUID userId, String title, boolean defaultShipping, boolean defaultBilling) {
        return UserAddress.create(
                userId,
                title,
                AddressType.BOTH,
                "Oytun Coban",
                "+905551112233",
                "Street 1",
                null,
                "Kadikoy",
                "Istanbul",
                "Türkiye",
                "34710",
                defaultShipping,
                defaultBilling
        );
    }
}
