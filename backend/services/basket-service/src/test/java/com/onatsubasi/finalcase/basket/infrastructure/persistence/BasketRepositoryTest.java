package com.onatsubasi.finalcase.basket.infrastructure.persistence;

import com.onatsubasi.finalcase.basket.AbstractIntegrationTest;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BasketRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SpringDataBasketJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("findByUserIdAndStatus returns the active basket with its items")
    void shouldFindActiveBasketByUserId() {
        UUID userId = UUID.randomUUID();
        Basket basket = Basket.empty(userId);
        basket.addItem(UUID.randomUUID(), 2);
        repository.saveAndFlush(basket);

        Optional<Basket> found = repository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().totalQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("partial unique index allows only one ACTIVE basket per user")
    void shouldEnforceOneActiveBasketPerUser() {
        UUID userId = UUID.randomUUID();
        repository.saveAndFlush(Basket.empty(userId));

        assertThatThrownBy(() -> repository.saveAndFlush(Basket.empty(userId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("same user can have a new active basket after previous one is checked out")
    void shouldAllowNewActiveBasketAfterCheckout() {
        UUID userId = UUID.randomUUID();
        Basket oldBasket = Basket.empty(userId);
        oldBasket.addItem(UUID.randomUUID(), 1);
        oldBasket.markCheckedOut(UUID.randomUUID());
        repository.saveAndFlush(oldBasket);

        Basket newBasket = repository.saveAndFlush(Basket.empty(userId));

        assertThat(newBasket.getStatus()).isEqualTo(BasketStatus.ACTIVE);
        assertThat(repository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)).isPresent();
    }

    @Test
    @DisplayName("findByIdForUpdate loads a basket for lifecycle mutations")
    void shouldFindByIdForUpdate() {
        UUID userId = UUID.randomUUID();
        Basket basket = repository.saveAndFlush(Basket.empty(userId));

        Optional<Basket> found = repository.findByIdForUpdate(basket.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }
    @Test
    @DisplayName("Flyway schema contains basket lifecycle columns used by the entity")
    void shouldApplyFlywaySchemaWithLifecycleColumns() {
        Integer columnCount = jdbcTemplate.queryForObject(
                """
                select count(*)
                  from information_schema.columns
                 where table_name = 'baskets'
                   and column_name in ('checked_out_at', 'cleared_at', 'abandoned_at', 'coupon_code_intent')
                """,
                Integer.class
        );

        assertThat(columnCount).isEqualTo(4);
    }

}
