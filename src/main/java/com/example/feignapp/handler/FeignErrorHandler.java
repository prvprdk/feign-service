package com.example.feignapp.handler;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class FeignErrorHandler implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {

        List<Integer> httpStatuses = Arrays.asList(
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.GATEWAY_TIMEOUT.value(),
                HttpStatus.INSUFFICIENT_STORAGE.value(),
                HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value(),
                HttpStatus.NOT_EXTENDED.value());

        if (httpStatuses.contains(response.status())) {
            throw new RetryableException(
                    response.status(),
                    String.format("%s : %s", s, response.status()),
                    response.request().httpMethod(),
                    (Long) null,
                    response.request());
        }

        log.error("Code: {}. Body: {} ", response.status(), response.body().toString());
        return new RuntimeException();
    }
}
