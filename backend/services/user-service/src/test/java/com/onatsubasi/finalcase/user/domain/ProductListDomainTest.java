package com.onatsubasi.finalcase.user.domain;

import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductListDomainTest {

    @Test
    @DisplayName("adding same product twice updates note without creating duplicate item")
    void addDuplicateProductUpdatesNoteOnly() {
        ProductList list = ProductList.create(UUID.randomUUID(), "Wishlist", null, ProductListVisibility.PRIVATE);
        UUID productId = UUID.randomUUID();

        boolean firstAdd = list.addItem(productId, "first note");
        boolean secondAdd = list.addItem(productId, "updated note");

        assertThat(firstAdd).isTrue();
        assertThat(secondAdd).isFalse();
        assertThat(list.getItems()).hasSize(1);
        assertThat(list.getItems().get(0).getNote()).isEqualTo("updated note");
    }

    @Test
    @DisplayName("removing missing product list item is safe and does not mutate collection")
    void removeMissingItemIsNoOp() {
        ProductList list = ProductList.create(UUID.randomUUID(), "Gift ideas", null, ProductListVisibility.PRIVATE);

        boolean removed = list.removeItem(UUID.randomUUID());

        assertThat(removed).isFalse();
        assertThat(list.getItems()).isEmpty();
    }
}
