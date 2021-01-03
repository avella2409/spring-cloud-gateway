# Spring cloud gateway

## Goal

Use spring cloud gateway with eureka service discovery to load balance request.

To demonstrate how to implement a super simple gateway we will have one eureka server (In production it is better to have multiple eureka server to have high availability), one eureka client with simple endpoint returning a UUID, we will launch two instance of this service to verify our load balancing work by looking at the UUID returned by the endpoint and finally we will have our gateway.

In our example we will use the gateway only to modify the path of the request and forward it to other service with load balancing, but we can do so much more with spring cloud gateway. Here are some features :
- Able to match routes on any request attribute.
- Predicates and filters are specific to routes.
- Circuit Breaker integration.
- Spring Cloud DiscoveryClient integration
- Easy to write Predicates and Filters
- Request Rate Limiting
- Path Rewriting

Official documentation : [Documentation](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/)

## Steps

### Step 1 : Create the eureka server

Add the `@EnableEurekaServer` annotation.

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
```

We only use one eureka server, so we specify to not try to register with others eureka server.

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Step 2 : Create a simple eureka client

We add the `@EnableEurekaClient` annotation to make the application register to the eureka server, and we create a simple endpoint that return a UUID and another endpoint that return something we specify in the path and the UUID.

```java
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
```

We specify the name of the application in the properties file, it will be used by other service to discover the location of this service using the eureka server registry.

```properties
spring.application.name=eureka-client
```

### Step 3 : Create the gateway

```java
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
```

We want to use the spring cloud load balancer that use reactor and not netflix ribbon, so we specify the property : `spring.cloud.loadbalancer.ribbon.enabled=false` 

```properties
spring.application.name=spring-cloud-gateway
server.port=9999
spring.cloud.loadbalancer.ribbon.enabled=false
```

### Step 4 : Try the gateway

- Launch the eureka server
- Launch two instance of our simple eureka client on different port
- Launch the gateway

If we go to `/test-id` and refresh multiple time we should see the UUID of our eureka client changing between both instance, it means the load balancing work and also that our gateway successfully forward request.

Now we try to go to `/test-print/hello` and again if we refresh multiple time we should see the message `hello` with the UUID of the eureka client changing between both instance.
