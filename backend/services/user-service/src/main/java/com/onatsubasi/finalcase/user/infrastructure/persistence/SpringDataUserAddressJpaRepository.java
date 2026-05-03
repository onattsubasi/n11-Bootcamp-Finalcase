package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserAddressJpaRepository extends JpaRepository<UserAddress, UUID> {

    Optional<UserAddress> findByIdAndUserIdAndDeletedFalse(UUID addressId, UUID userId);

    List<UserAddress> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    @Query("""
           select a
             from UserAddress a
            where a.userId = :userId
              and a.deleted = false
              and a.defaultShipping = true
           """)
    Optional<UserAddress> findDefaultShippingAddress(@Param("userId") UUID userId);

    @Query("""
           select a
             from UserAddress a
            where a.userId = :userId
              and a.deleted = false
              and a.defaultBilling = true
           """)
    Optional<UserAddress> findDefaultBillingAddress(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update UserAddress a
              set a.defaultShipping = false,
                  a.updatedAt = :updatedAt
            where a.userId = :userId
              and a.deleted = false
              and a.defaultShipping = true
           """)
    void clearDefaultShipping(@Param("userId") UUID userId, @Param("updatedAt") java.time.Instant updatedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update UserAddress a
              set a.defaultBilling = false,
                  a.updatedAt = :updatedAt
            where a.userId = :userId
              and a.deleted = false
              and a.defaultBilling = true
           """)
    void clearDefaultBilling(@Param("userId") UUID userId, @Param("updatedAt") java.time.Instant updatedAt);
}
