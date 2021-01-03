package com.avella.sample.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(routeSpec -> routeSpec.method(HttpMethod.GET)
                        .and()
                        .path("/test-id")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec.setPath("/id"))
                        .uri("lb://eureka-client/"))
                .route(routeSpec -> routeSpec.method(HttpMethod.GET)
                        .and()
                        .path("/test-print/{msg}")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec.setPath("/print/{msg}"))
                        .uri("lb://eureka-client/"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
