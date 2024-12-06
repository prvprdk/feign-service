package com.example.feignapp.feign;

import com.example.feignapp.config.FeignClientConfig;
import com.example.feignapp.handler.FeignErrorHandler;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import feign.RetryableException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest(classes = {
        HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class,
        ExternalClient.class,
        FeignClientConfig.class,
        FeignErrorHandler.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableFeignClients
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class ExternalClientRetryerTest {
    @Autowired
    ExternalClient externalClient;
    @Autowired
    WireMockServer mockServer;

    @Value("${spring.feign.client.config.default.retry.max-attempts}")
    int retries;

    @ParameterizedTest
    @MethodSource("httpStatuses")
    void checkShouldRetry_whenFeignClientNotAvailable(int httpStatus) {

        //given
        mockServer.stubFor(WireMock.get(urlEqualTo("/message"))

                .willReturn(WireMock.aResponse()
                        .withStatus(httpStatus)));

        //when
        Assertions.assertThrows(RetryableException.class, () -> externalClient.get());

        //then
        mockServer.verify(retries, RequestPatternBuilder.newRequestPattern()
                .withUrl("/message")
        );

        mockServer.resetAll();
    }

    private static Set<Integer> httpStatuses() {
        return Set.of(
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.GATEWAY_TIMEOUT.value(),
                HttpStatus.INSUFFICIENT_STORAGE.value(),
                HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value(),
                HttpStatus.NOT_EXTENDED.value());
    }
}
