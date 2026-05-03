package com.onatsubasi.finalcase.user.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAddressDomainTest {

    @Test
    @DisplayName("billing-only address is not usable as shipping until explicitly converted")
    void billingOnlyAddressCanBeConvertedToBothWhenMarkedDefaultShipping() {
        UserAddress address = address(AddressType.BILLING, false, true);

        assertThat(address.canBeUsedAsShipping()).isFalse();

        address.markDefaultShipping();

        assertThat(address.getType()).isEqualTo(AddressType.BOTH);
        assertThat(address.canBeUsedAsShipping()).isTrue();
        assertThat(address.isDefaultShipping()).isTrue();
    }

    @Test
    @DisplayName("soft delete clears default flags and hides address from snapshot eligibility")
    void softDeleteClearsDefaults() {
        UserAddress address = address(AddressType.BOTH, true, true);

        address.softDelete();

        assertThat(address.isDeleted()).isTrue();
        assertThat(address.isDefaultShipping()).isFalse();
        assertThat(address.isDefaultBilling()).isFalse();
        assertThat(address.canBeUsedAsShipping()).isFalse();
        assertThat(address.canBeUsedAsBilling()).isFalse();
    }

    @Test
    @DisplayName("address ownership failure is hidden as not found/access denied")
    void assertOwnedByRejectsDifferentUser() {
        UserAddress address = address(AddressType.SHIPPING, false, false);

        assertThatThrownBy(() -> address.assertOwnedBy(UUID.randomUUID()))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.ADDRESS_ACCESS_DENIED);
    }

    private UserAddress address(AddressType type, boolean defaultShipping, boolean defaultBilling) {
        return UserAddress.create(
                UUID.randomUUID(),
                "Home",
                type,
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
