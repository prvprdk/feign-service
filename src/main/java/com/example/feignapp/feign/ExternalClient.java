package com.example.feignapp.feign;


import com.example.feignapp.domain.Message;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "external-client")
public interface ExternalClient {

    @GetMapping
    Message get();
}
