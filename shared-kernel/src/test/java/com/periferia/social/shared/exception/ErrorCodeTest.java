package com.periferia.social.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrorCodeTest {

    @ParameterizedTest
    @CsvSource({
            "400, VALIDATION_ERROR",
            "401, UNAUTHORIZED",
            "404, RESOURCE_NOT_FOUND",
            "409, CONFLICT",
            "502, INTERNAL_ERROR",
            "503, SERVICE_UNAVAILABLE"
    })
    void mapsHttpStatusToErrorCode(int status, ErrorCode expected) {
        assertThat(ErrorCode.fromHttpStatus(status)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"INVALID_CREDENTIALS, 401", "IDEMPOTENCY_KEY_REUSED, 409", "BUSINESS_RULE_VIOLATION, 422"})
    void exposesSuggestedHttpStatus(ErrorCode code, int expected) {
        assertThat(code.httpStatus()).isEqualTo(expected);
    }
}
