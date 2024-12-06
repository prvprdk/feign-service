package com.example.feignapp.feign;


import com.example.feignapp.domain.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = {
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        ExternalClient.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableFeignClients
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class ExternalClientTest {

    @Autowired
    WireMockServer wireMockServer;
    @Autowired
    ExternalClient externalClient;

    @Test
    void getShouldReturnMessage_whenFeignClientAvailable() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        Message message = new Message("test message");
        String jsonMessage = objectMapper.writeValueAsString(message);

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/message"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonMessage)));

        Message actualMessage = externalClient.get();

        Assertions.assertEquals(message, actualMessage);

    }
}
