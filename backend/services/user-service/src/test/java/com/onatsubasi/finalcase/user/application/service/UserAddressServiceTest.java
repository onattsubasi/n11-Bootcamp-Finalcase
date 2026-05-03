package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotRequest;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotResponse;
import com.onatsubasi.finalcase.user.application.dto.request.CreateAddressRequest;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.repository.UserAddressRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import com.onatsubasi.finalcase.user.support.TestUserContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressRepository addressRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    private UserAddressService service;

    private UUID userId;
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        service = new UserAddressService(addressRepository, new UserMapper(), eventPublisher);
        userId = UUID.randomUUID();
        userContext = TestUserContexts.customer(userId, "user@example.com");
    }

    @Test
    @DisplayName("create address clears previous defaults only when request asks for defaults")
    void createAddressClearsRequestedDefaults() {
        CreateAddressRequest request = new CreateAddressRequest(
                "Home",
                AddressType.BOTH,
                "Oytun Coban",
                "+905551112233",
                "Street 1",
                null,
                "Kadikoy",
                "Istanbul",
                "Türkiye",
                "34710",
                true,
                true
        );
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAddress(userContext, request);

        verify(addressRepository).clearDefaultShipping(eq(userId), any(Instant.class));
        verify(addressRepository).clearDefaultBilling(eq(userId), any(Instant.class));
        verify(eventPublisher).publishAddressCreated(any(UserAddress.class));
    }

    @Test
    @DisplayName("mark default shipping rejects billing-only address before mutating defaults")
    void markDefaultShippingRejectsBillingOnlyAddress() {
        UUID addressId = UUID.randomUUID();
        UserAddress billingOnly = address(AddressType.BILLING, false, true);
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(addressId, userId))
                .thenReturn(Optional.of(billingOnly));

        assertThatThrownBy(() -> service.markDefaultShipping(userContext, addressId))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_ADDRESS_DATA);

        verify(addressRepository, never()).clearDefaultShipping(any(), any());
        verify(eventPublisher, never()).publishAddressUpdated(any());
    }

    @Test
    @DisplayName("address snapshot uses shipping address as billing when billing id is omitted")
    void snapshotUsesShippingAddressForBillingWhenOmitted() {
        UUID addressId = UUID.randomUUID();
        UserAddress both = address(AddressType.BOTH, true, true);
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(addressId, userId))
                .thenReturn(Optional.of(both));

        AddressSnapshotResponse response = service.getAddressSnapshots(
                new AddressSnapshotRequest(userId, addressId, null)
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.shippingAddress().recipientName()).isEqualTo("Oytun Coban");
        assertThat(response.billingAddress().recipientName()).isEqualTo("Oytun Coban");
        verify(addressRepository, times(1)).findByIdAndUserIdAndDeletedFalse(addressId, userId);
    }

    @Test
    @DisplayName("address snapshot does not leak another user's address existence")
    void snapshotRejectsMissingOwnedAddress() {
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findByIdAndUserIdAndDeletedFalse(addressId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAddressSnapshots(new AddressSnapshotRequest(userId, addressId, null)))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.ADDRESS_NOT_FOUND);
    }

    private UserAddress address(AddressType type, boolean defaultShipping, boolean defaultBilling) {
        return UserAddress.create(
                userId,
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
