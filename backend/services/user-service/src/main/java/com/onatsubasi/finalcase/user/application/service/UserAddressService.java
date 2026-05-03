package com.onatsubasi.finalcase.user.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshot;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotRequest;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotResponse;
import com.onatsubasi.finalcase.user.application.dto.request.CreateAddressRequest;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateAddressRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserAddressResponse;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.exception.UserErrorCode;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.repository.UserAddressRepository;
import com.onatsubasi.finalcase.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository addressRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<UserAddressResponse> listMyAddresses(UserContext userContext) {
        UUID userId = requireUserId(userContext);

        return addressRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserAddressResponse createAddress(
            UserContext userContext,
            CreateAddressRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.address.create.started");
            MDC.put("userId", userId.toString());

            if (request.defaultShipping()) {
                addressRepository.clearDefaultShipping(userId, Instant.now());
            }

            if (request.defaultBilling()) {
                addressRepository.clearDefaultBilling(userId, Instant.now());
            }

            UserAddress address = UserAddress.create(
                    userId,
                    request.title(),
                    request.type(),
                    request.recipientName(),
                    request.phoneNumber(),
                    request.line1(),
                    request.line2(),
                    request.district(),
                    request.city(),
                    request.country(),
                    request.postalCode(),
                    request.defaultShipping(),
                    request.defaultBilling()
            );

            UserAddress saved = addressRepository.save(address);
            eventPublisher.publishAddressCreated(saved);

            MDC.put("eventName", "user.address.created");
            log.info("User address created, userId={}, addressId={}", userId, saved.getId());

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.address.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public UserAddressResponse updateAddress(
            UserContext userContext,
            UUID addressId,
            UpdateAddressRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.address.update.started");
            MDC.put("userId", userId.toString());

            UserAddress address = getOwnedAddress(userId, addressId);

            if (request.defaultShipping()) {
                addressRepository.clearDefaultShipping(userId, Instant.now());
            }

            if (request.defaultBilling()) {
                addressRepository.clearDefaultBilling(userId, Instant.now());
            }

            address.update(
                    request.title(),
                    request.type(),
                    request.recipientName(),
                    request.phoneNumber(),
                    request.line1(),
                    request.line2(),
                    request.district(),
                    request.city(),
                    request.country(),
                    request.postalCode(),
                    request.defaultShipping(),
                    request.defaultBilling()
            );

            UserAddress saved = addressRepository.save(address);
            eventPublisher.publishAddressUpdated(saved);

            MDC.put("eventName", "user.address.updated");
            log.info("User address updated, userId={}, addressId={}", userId, saved.getId());

            return userMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("user.address.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void deleteAddress(UserContext userContext, UUID addressId) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "user.address.delete.started");
            MDC.put("userId", userId.toString());

            UserAddress address = getOwnedAddress(userId, addressId);
            address.softDelete();

            UserAddress saved = addressRepository.save(address);
            eventPublisher.publishAddressDeleted(saved);

            MDC.put("eventName", "user.address.deleted");
            log.info("User address deleted, userId={}, addressId={}", userId, saved.getId());
        } catch (BaseException ex) {
            logBusinessFailure("user.address.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public UserAddressResponse markDefaultShipping(UserContext userContext, UUID addressId) {
        UUID userId = requireUserId(userContext);

        UserAddress address = getOwnedAddress(userId, addressId);

        if (!address.canBeUsedAsShipping()) {
            throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, "Address cannot be used as shipping address");
        }

        addressRepository.clearDefaultShipping(userId, Instant.now());
        address.markDefaultShipping();

        UserAddress saved = addressRepository.save(address);
        eventPublisher.publishAddressUpdated(saved);

        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserAddressResponse markDefaultBilling(UserContext userContext, UUID addressId) {
        UUID userId = requireUserId(userContext);

        UserAddress address = getOwnedAddress(userId, addressId);

        if (!address.canBeUsedAsBilling()) {
            throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, "Address cannot be used as billing address");
        }

        addressRepository.clearDefaultBilling(userId, Instant.now());
        address.markDefaultBilling();

        UserAddress saved = addressRepository.save(address);
        eventPublisher.publishAddressUpdated(saved);

        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AddressSnapshotResponse getAddressSnapshots(AddressSnapshotRequest request) {
        UUID userId = request.userId();

        if (userId == null) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        MDC.put("eventName", "user.address_snapshot.requested");
        MDC.put("userId", userId.toString());

        try {
            UserAddress shippingAddress = addressRepository
                    .findByIdAndUserIdAndDeletedFalse(request.shippingAddressId(), userId)
                    .orElseThrow(() -> new BaseException(UserErrorCode.ADDRESS_NOT_FOUND));

            if (!shippingAddress.canBeUsedAsShipping()) {
                throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, "Address cannot be used as shipping address");
            }

            UserAddress billingAddress = request.billingAddressId() == null
                    ? shippingAddress
                    : addressRepository.findByIdAndUserIdAndDeletedFalse(request.billingAddressId(), userId)
                            .orElseThrow(() -> new BaseException(UserErrorCode.ADDRESS_NOT_FOUND));

            if (!billingAddress.canBeUsedAsBilling()) {
                throw new BaseException(UserErrorCode.INVALID_ADDRESS_DATA, "Address cannot be used as billing address");
            }

            AddressSnapshot shippingSnapshot = userMapper.toSnapshot(shippingAddress);
            AddressSnapshot billingSnapshot = userMapper.toSnapshot(billingAddress);

            log.info(
                    "User address snapshot created, userId={}, shippingAddressId={}, billingAddressId={}",
                    userId,
                    shippingAddress.getId(),
                    billingAddress.getId()
            );

            return new AddressSnapshotResponse(
                    userId,
                    shippingSnapshot,
                    billingSnapshot,
                    Instant.now()
            );
        } finally {
            MDC.remove("eventName");
            MDC.remove("userId");
        }
    }

    private UserAddress getOwnedAddress(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserIdAndDeletedFalse(addressId, userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.ADDRESS_NOT_FOUND));
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(UserErrorCode.INVALID_USER_ID);
        }

        return userContext.userId();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("User address operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("userId");
    }
}
