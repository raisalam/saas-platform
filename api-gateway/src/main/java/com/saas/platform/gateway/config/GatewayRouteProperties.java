package com.saas.platform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.routes")
public class GatewayRouteProperties {

    private String userServiceUrl;
    private String catalogServiceUrl;

    // ----- getters -----

    public String getUserServiceUrl() {
        return userServiceUrl;
    }

    public String getCatalogServiceUrl() {
        return catalogServiceUrl;
    }

    // ----- setters -----

    public void setUserServiceUrl(String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }

    public void setCatalogServiceUrl(String catalogServiceUrl) {
        this.catalogServiceUrl = catalogServiceUrl;
    }
}
