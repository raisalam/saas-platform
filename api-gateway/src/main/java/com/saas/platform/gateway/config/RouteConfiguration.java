package com.saas.platform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static com.saas.platform.gateway.filter.JwtFilterFunctions.authenticate;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.*;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static com.saas.platform.gateway.functions.CorrelationBeforeFilterFunctions.instrument;
@Configuration
class RouteConfiguration {

    private final GatewayRouteProperties props;

    RouteConfiguration(GatewayRouteProperties props) {
        this.props = props;
    }
    @Bean
    public RouterFunction<ServerResponse> instrumentRoute() {

        RouterFunction<ServerResponse> userRoute = route("user-service")
                .path("/api/user/**", builder ->
                        builder
                                .filter(authenticate())
                                .before(instrument())
                                .before(uri(props.getUserServiceUrl()))
                                .route(req -> true, http())
                )
                .build();

        RouterFunction<ServerResponse> catalogRoute = route("catalog-service")
                .path("/api/catalog/**", builder ->
                        builder
                                .filter(authenticate())
                                .before(instrument())
                                .before(uri(props.getCatalogServiceUrl()))
                                .route(req -> true, http())
                )
                .build();

        return userRoute.and(catalogRoute);  // combine both routes
    }
}
