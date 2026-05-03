package com.onatsubasi.finalcase.common.core.exception;

public interface ErrorCode {

    String code();

    String defaultMessage();

    int httpStatus();
}