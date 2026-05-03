package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class CheckoutDownstreamResponseExtractor {

    public <T> T extract(
            ApiResponse<T> response,
            CheckoutErrorCode errorCode
    ) {
        if (response == null || response.data() == null) {
            throw new BaseException(errorCode);
        }

        return response.data();
    }
}