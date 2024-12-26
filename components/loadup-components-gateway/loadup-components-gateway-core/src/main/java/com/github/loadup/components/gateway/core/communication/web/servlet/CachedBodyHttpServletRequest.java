package com.github.loadup.components.gateway.core.communication.web.servlet;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 缓存请求体
        try (InputStream inputStream = request.getInputStream()) {
            this.cachedBody = StreamUtils.copyToByteArray(inputStream);
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        // 返回一个新的流，包装缓存的字节数组
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        return new CachedBodyServletInputStream(byteArrayInputStream);
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }
}
