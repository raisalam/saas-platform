package com.saas.platform.gateway.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ModifiedRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public ModifiedRequestWrapper(HttpServletRequest request, String newBody) {
        super(request);                    // ONLY ONE ARGUMENT — CORRECT
        this.body = newBody.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {
            private final ByteArrayInputStream bis = new ByteArrayInputStream(body);

            @Override public boolean isFinished() { return bis.available() == 0; }
            @Override public boolean isReady() { return true; }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override public int read() throws IOException { return bis.read(); }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }
}