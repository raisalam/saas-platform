// src/main/java/com/example/demo/filter/JwtFilterSupplier.java
package com.saas.platform.gateway.supplier;

import com.saas.platform.gateway.filter.JwtFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.SimpleFilterSupplier;

public class JwtFilterSupplier extends SimpleFilterSupplier {
    public JwtFilterSupplier() {
        super(JwtFilterFunctions.class);
    }
}