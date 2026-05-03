package com.onatsubasi.finalcase.payment.infrastructure.provider.iyzico;

import com.iyzipay.model.*;
import com.iyzipay.request.*;
import com.onatsubasi.finalcase.payment.application.dto.provider.*;
import com.onatsubasi.finalcase.payment.infrastructure.config.IyzicoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IyzicoPaymentMapper {

    private final IyzicoProperties properties;

    public CreateCheckoutFormInitializeRequest toCheckoutFormInitializeRequest(
            ProviderPaymentInitializeCommand command
    ) {
        CreateCheckoutFormInitializeRequest request =
                new CreateCheckoutFormInitializeRequest();

        request.setLocale(properties.getLocale());
        request.setConversationId(command.paymentAttemptId().toString());
        request.setPrice(command.amount());
        request.setPaidPrice(command.paidAmount());
        request.setCurrency(command.currency());
        request.setBasketId(command.basketId() == null
                ? command.orderId().toString()
                : command.basketId().toString());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setCallbackUrl(command.callbackUrl());

        request.setBuyer(toBuyer(command));
        request.setShippingAddress(toAddress(command.shippingAddress()));
        request.setBillingAddress(toAddress(command.billingAddress()));
        request.setBasketItems(toBasketItems(command.basketItems()));

        return request;
    }

    public RetrieveCheckoutFormRequest toRetrieveCheckoutFormRequest(
            ProviderPaymentRetrieveCommand command
    ) {
        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(properties.getLocale());
        request.setConversationId(command.paymentAttemptId().toString());
        request.setToken(command.providerToken());
        return request;
    }

    public CreateRefundRequest toRefundRequest(ProviderRefundCommand command) {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setLocale(properties.getLocale());
        request.setConversationId(command.refundId().toString());
        request.setPaymentTransactionId(command.providerTransactionId());
        request.setPrice(command.amount());
        request.setCurrency(command.currency());
        return request;
    }

    public CreateCancelRequest toCancelRequest(ProviderCancelCommand command) {
        CreateCancelRequest request = new CreateCancelRequest();
        request.setLocale(properties.getLocale());
        request.setConversationId(command.cancellationId().toString());
        request.setPaymentId(command.providerPaymentId());
        return request;
    }

    private Buyer toBuyer(ProviderPaymentInitializeCommand command) {
        ProviderBuyerInfo source = command.buyer();

        Buyer buyer = new Buyer();
        buyer.setId(valueOrDefault(source.id(), command.userId().toString()));
        buyer.setName(valueOrDefault(source.name(), "Customer"));
        buyer.setSurname(valueOrDefault(source.surname(), "User"));
        buyer.setGsmNumber(source.phone());
        buyer.setEmail(source.email());
        buyer.setIdentityNumber(valueOrDefault(
                source.identityNumber(),
                properties.getDefaultIdentityNumber()
        ));
        buyer.setLastLoginDate(null);
        buyer.setRegistrationDate(null);
        buyer.setRegistrationAddress(valueOrDefault(
                source.registrationAddress(),
                command.billingAddress().address()
        ));
        buyer.setIp(valueOrDefault(source.ip(), command.clientIp()));
        buyer.setCity(valueOrDefault(source.city(), command.billingAddress().city()));
        buyer.setCountry(valueOrDefault(source.country(), properties.getDefaultCountry()));
        buyer.setZipCode(valueOrDefault(source.zipCode(), command.billingAddress().zipCode()));

        return buyer;
    }

    private Address toAddress(ProviderAddressInfo source) {
        Address address = new Address();
        address.setContactName(valueOrDefault(source.contactName(), "Customer"));
        address.setCity(source.city());
        address.setCountry(valueOrDefault(source.country(), properties.getDefaultCountry()));
        address.setAddress(source.address());
        address.setZipCode(source.zipCode());
        return address;
    }

    private List<BasketItem> toBasketItems(List<ProviderBasketItemInfo> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(this::toBasketItem)
                .toList();
    }

    private BasketItem toBasketItem(ProviderBasketItemInfo source) {
        BasketItem item = new BasketItem();
        item.setId(source.id());
        item.setName(source.name());
        item.setCategory1(valueOrDefault(source.categoryName(), "General"));
        item.setCategory2(null);
        item.setItemType(normalizeItemType(source.itemType()));
        item.setPrice(source.price());
        return item;
    }

    private String normalizeItemType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return BasketItemType.PHYSICAL.name();
        }

        if ("VIRTUAL".equalsIgnoreCase(itemType)) {
            return BasketItemType.VIRTUAL.name();
        }

        return BasketItemType.PHYSICAL.name();
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim();
    }
}