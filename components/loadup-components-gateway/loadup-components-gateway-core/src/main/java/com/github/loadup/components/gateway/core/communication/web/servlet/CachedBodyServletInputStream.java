package com.github.loadup.components.gateway.core.communication.web.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CachedBodyServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream byteArrayInputStream;

    public CachedBodyServletInputStream(ByteArrayInputStream byteArrayInputStream) {
        this.byteArrayInputStream = byteArrayInputStream;
    }

    @Override
    public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        // No-op: 非异步流，不支持监听器
    }

    @Override
    public int read() throws IOException {
        return byteArrayInputStream.read();
    }
}