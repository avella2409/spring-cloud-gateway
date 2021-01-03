package com.avella.sample.eurekaclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@SpringBootApplication
@EnableEurekaClient
public class EurekaClientApplication {

    private final String id = UUID.randomUUID().toString();

    @Bean
    RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .GET("/id", serverRequest -> ServerResponse.ok().body(Mono.just(id), String.class))
                .GET("/print/{msg}", serverRequest -> ServerResponse.ok()
                        .body(Mono.just(serverRequest.pathVariable("msg") + " " + id), String.class))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }

}
