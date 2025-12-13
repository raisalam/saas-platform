package com.saas.platform.gateway.functions;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public class CorrelationBeforeFilterFunctions {
    public static Function<ServerRequest, ServerRequest> instrument() {
        return request -> {
            return ServerRequest.from(request)
                    .header("X-Trace-Id", UUID.randomUUID().toString())
                    .header("X-Gateway-Id", "841428-001") // your internal ID
                    .build();
        };
    }
}