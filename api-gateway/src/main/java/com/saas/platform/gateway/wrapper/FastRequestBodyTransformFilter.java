package com.saas.platform.gateway.wrapper;

import com.saas.platform.gateway.util.RequestDecrypter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FastRequestBodyTransformFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FastRequestBodyTransformFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String deviceId = request.getHeader("device-id");
        if (deviceId == null || deviceId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Always cache response so we can read + rewrite it
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Decrypt request → return either original or modified wrapper
        HttpServletRequest requestToUse = decryptRequestIfNeeded(request, deviceId);

        try {
            filterChain.doFilter(requestToUse, responseWrapper);

            // Encrypt response only if it was JSON
            if (isJsonResponse(responseWrapper)) {
                encryptAndWriteResponse(responseWrapper, deviceId);
            } else {
                responseWrapper.copyBodyToResponse(); // passthrough non-JSON
            }

        } catch (Exception e) {
            log.warn("Processing failed during encryption/decryption", e);
            try {
                responseWrapper.copyBodyToResponse();
            } catch (Exception ignored) {}
        }
    }

    private HttpServletRequest decryptRequestIfNeeded(HttpServletRequest request, String deviceId) {
        if (!isJsonRequest(request)) {
            return request;
        }

        try {
            // Read body once — safe because we're in a filter and haven't passed it downstream yet
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

            if (body.isBlank()) return request;

            var root = RequestDecrypter.MAPPER.readTree(body);
            var dataNode = root.get("data");

            if (dataNode == null || !dataNode.isTextual()) {
                return request; // not encrypted payload
            }

            String encrypted = dataNode.asText();
            var decrypted = RequestDecrypter.decryptRequest(encrypted, deviceId);
            String newBody = RequestDecrypter.MAPPER.writeValueAsString(decrypted);

            // This is your working wrapper — 100% compatible
            return new ModifiedRequestWrapper(request, newBody);

        } catch (Exception e) {
            log.debug("Request decryption failed or not applicable", e);
            return request; // fallback: send original
        }
    }

    private void encryptAndWriteResponse(ContentCachingResponseWrapper wrapper, String deviceId) throws IOException {
        byte[] bodyBytes = wrapper.getContentAsByteArray();
        if (bodyBytes.length == 0) {
            wrapper.copyBodyToResponse();
            return;
        }

        try {
            String body = new String(bodyBytes, wrapper.getCharacterEncoding());
            String encrypted = RequestDecrypter.encryptRequest(body, deviceId);

            // Fast & safe JSON wrapper
            String encryptedResponse = "{\"data\":\"" + encrypted.replace("\"", "\\\"") + "\"}";
            byte[] output = encryptedResponse.getBytes(StandardCharsets.UTF_8);

            HttpServletResponse raw = (HttpServletResponse) wrapper.getResponse();
            raw.resetBuffer(); // important!
            raw.setContentLength(output.length);
            raw.setContentType(MediaType.APPLICATION_JSON_VALUE);
            raw.getOutputStream().write(output);

        } catch (Exception e) {
            log.error("Response encryption failed — sending original", e);
            wrapper.copyBodyToResponse();
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String ct = request.getContentType();
        return ct != null && ct.toLowerCase().contains("application/json");
    }

    private boolean isJsonResponse(ContentCachingResponseWrapper wrapper) {
        String ct = wrapper.getContentType();
        return ct != null && ct.toLowerCase().contains("application/json");
    }
}