package io.github.loadup.components.http;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

/**
 * Measures the native lease boundary without including connect or response time.
 */
final class MeteredConnectionManager implements HttpClientConnectionManager {
    private final HttpClientConnectionManager delegate;
    private final HttpPoolMetrics.State metrics;

    MeteredConnectionManager(HttpClientConnectionManager delegate, HttpPoolMetrics.State metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
    }

    @Override
    public LeaseRequest lease(String id, HttpRoute route, Timeout requestTimeout, Object state) {
        long start = metrics.start();
        LeaseRequest lease;
        try {
            lease = delegate.lease(id, route, requestTimeout, state);
        } catch (RuntimeException failure) {
            metrics.record(start, "failure");
            throw failure;
        }
        return new LeaseRequest() {
            private final AtomicBoolean recorded = new AtomicBoolean();

            @Override
            public ConnectionEndpoint get(Timeout timeout)
                throws InterruptedException, ExecutionException, TimeoutException {
                String outcome = "failure";
                try {
                    ConnectionEndpoint endpoint = lease.get(timeout);
                    outcome = "success";
                    return endpoint;
                } catch (TimeoutException failure) {
                    outcome = "timeout";
                    throw failure;
                } catch (InterruptedException failure) {
                    outcome = "interrupted";
                    throw failure;
                } finally {
                    if (recorded.compareAndSet(false, true)) {
                        metrics.record(start, outcome);
                    }
                }
            }

            @Override
            public boolean cancel() {
                return lease.cancel();
            }
        };
    }

    @Override
    public void release(ConnectionEndpoint endpoint, Object state, TimeValue validDuration) {
        delegate.release(endpoint, state, validDuration);
    }

    @Override
    public void connect(ConnectionEndpoint endpoint, TimeValue connectTimeout, HttpContext context) throws IOException {
        delegate.connect(endpoint, connectTimeout, context);
    }

    @Override
    public void upgrade(ConnectionEndpoint endpoint, HttpContext context) throws IOException {
        delegate.upgrade(endpoint, context);
    }

    @Override
    public void close(CloseMode closeMode) {
        delegate.close(closeMode);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
