package io.github.loadup.components.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Standard Spring proxies resolve the current managed transport for every invocation.
 */
final class HttpServiceClients {
    private HttpServiceClients() {}

    static <T> T create(HttpTemplate template, String name, Class<T> serviceType) {
        if (serviceType == null
                || !serviceType.isInterface()
                || !java.lang.reflect.Modifier.isPublic(serviceType.getModifiers())) {
            throw new IllegalArgumentException("HTTP service type must be a public interface");
        }
        // Validate configuration now; do not retain a pool that refresh may close later.
        template.withServiceClient(name, serviceType, client -> Boolean.TRUE);
        Object proxy = Proxy.newProxyInstance(
                serviceType.getClassLoader(), new Class<?>[] {serviceType}, (self, method, arguments) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "HTTP Service Client [" + serviceType.getSimpleName() + "]";
                            case "hashCode" -> System.identityHashCode(self);
                            case "equals" -> self == arguments[0];
                            default -> throw new IllegalStateException("Unexpected object method");
                        };
                    }
                    return template.withServiceClient(name, serviceType, target -> {
                        try {
                            return method.invoke(target, arguments);
                        } catch (InvocationTargetException failure) {
                            if (failure.getCause() instanceof RuntimeException runtime) {
                                throw runtime;
                            }
                            if (failure.getCause() instanceof Error error) {
                                throw error;
                            }
                            throw new IllegalStateException("HTTP service invocation failed");
                        } catch (IllegalAccessException failure) {
                            throw new IllegalStateException("HTTP service interface must be public");
                        }
                    });
                });
        return serviceType.cast(proxy);
    }

    /**
     * The transport response is fully bounded and closed before a converter receives it.
     */
    static final class BufferedResponse implements ClientHttpResponse {
        private final HttpStatusCode status;
        private final HttpHeaders headers;
        private final InputStream body;

        BufferedResponse(HttpStatusCode status, HttpHeaders headers, byte[] bytes) {
            this.status = status;
            HttpHeaders snapshot = new HttpHeaders();
            snapshot.addAll(headers);
            this.headers = HttpHeaders.readOnlyHttpHeaders(snapshot);
            this.body = new ByteArrayInputStream(bytes);
        }

        @Override
        public HttpStatusCode getStatusCode() {
            return status;
        }

        @Override
        public String getStatusText() {
            return "";
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public void close() {}
    }
}
