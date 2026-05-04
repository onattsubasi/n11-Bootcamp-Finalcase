package com.onatsubasi.finalcase.checkout.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutMapperTest {

    private final CheckoutMapper mapper = new CheckoutMapper(new ObjectMapper());

    @Test
    void toQuoteResponse_usesBasketCatalogAndPromotionDataForDeterministicTotals() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        BasketSnapshotClientResponse basket = basket(userId, basketId, productId, "1000.00", 2);
        List<CatalogProductSnapshotClientResponse> products = List.of(product(productId, "1000.00", true));
        PromotionQuoteClientResponse promotion = new PromotionQuoteClientResponse(
                money("2000.00"),
                money("150.00"),
                money("0.00"),
                money("1850.00"),
                List.of(new AppliedPromotionDiscountClientResponse(
                        UUID.randomUUID(),
                        "Welcome Promotion",
                        UUID.randomUUID(),
                        "WELCOME",
                        "FIXED_AMOUNT_DISCOUNT",
                        money("50.00"),
                        BigDecimal.ZERO,
                        "welcome discount"
                )));

        CheckoutQuoteResponse response = mapper.toQuoteResponse(basket, products, promotion, money("0.00"), money("0.00"));

        assertThat(response.basketId()).isEqualTo(basketId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productName()).isEqualTo("Example Product");
        assertThat(response.money().subtotalAmount()).isEqualByComparingTo("2000.00");
        assertThat(response.money().promotionDiscountAmount()).isEqualByComparingTo("150.00");
        assertThat(response.money().grandTotalAmount()).isEqualByComparingTo("1850.00");
    }

    @Test
    void applyCheckoutSnapshot_persistsItemAddressAndDiscountSnapshotsInSession() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();
        UUID addressId = UUID.randomUUID();

        BasketSnapshotClientResponse basket = basket(userId, basketId, productId, "500.00", 1);
        PromotionQuoteClientResponse promotion = new PromotionQuoteClientResponse(
                money("500.00"), money("50.00"), money("0.00"), money("450.00"),
                List.of(new AppliedPromotionDiscountClientResponse(
                        UUID.randomUUID(),
                        "Welcome Promotion",
                        UUID.randomUUID(),
                        "WELCOME",
                        "FIXED_AMOUNT_DISCOUNT",
                        money("50.00"),
                        BigDecimal.ZERO,
                        "welcome discount"
                )));
        CheckoutQuoteResponse quote = mapper.toQuoteResponse(basket, List.of(product(productId, "500.00", true)), promotion, money("0.00"), money("0.00"));
        CheckoutSession session = CheckoutSession.start(userId, "idem", "hash", "TRY", Instant.now().plusSeconds(1800));
        UserAddressSnapshotClientResponse address = address(addressId, userId);

        mapper.applyCheckoutSnapshot(session, quote, address, address);

        assertThat(session.getItems()).hasSize(1);
        assertThat(session.getAddresses()).hasSize(2);
        assertThat(session.getDiscounts()).hasSize(1);
        assertThat(session.getItems().getFirst().getProductId().toString()).isEqualTo(productId);
        assertThat(session.getDiscounts().getFirst().getDiscountAmount()).isEqualByComparingTo("50.00");
    }

    private static BasketSnapshotClientResponse basket(
            UUID userId,
            UUID basketId,
            String productId,
            String unitPrice,
            int quantity
    ) {
        BigDecimal price = money(unitPrice);

        return new BasketSnapshotClientResponse(
                basketId,
                userId,
                List.of(new BasketItemClientResponse(productId, "SKU-1", quantity, price, "TRY"))
        );
    }

    private static CatalogProductSnapshotClientResponse product(String productId, String price, boolean sellable) {
        return new CatalogProductSnapshotClientResponse(
                productId,
                "SKU-1",
                "Example Product",
                "example-product",
                sellable ? "ACTIVE" : "INACTIVE",
                sellable,
                new CatalogProductSnapshotClientResponse.MoneySnapshot(money(price), "TRY"),
                new CatalogProductSnapshotClientResponse.BrandSnapshot(UUID.randomUUID().toString(), "Brand", "brand"),
                new CatalogProductSnapshotClientResponse.CategorySnapshot(
                        UUID.randomUUID().toString(),
                        "Category",
                        "category",
                        "/category"
                ),
                new CatalogProductSnapshotClientResponse.OwnershipSnapshot("PLATFORM", null, null),
                "https://cdn.example.com/product.jpg"
        );
    }

    private static UserAddressSnapshotClientResponse address(UUID addressId, UUID userId) {
        return new UserAddressSnapshotClientResponse(
                addressId,
                userId,
                "Home",
                "Oytun Coban",
                "+905551112233",
                "Türkiye",
                "İstanbul",
                "Kadıköy",
                "Caferağa",
                "Example street no 1",
                "Floor 2",
                "34710"
        );
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
