package io.github.loadup.commons.util.internal;

/**
 * The internal data structure that stores the thread-local variables for Netty and all {@link
 * InternalThread}s. Note that this class is for internal use only. Use {@link
 * Thread#currentThread()} to get the current thread for a regular thread, and use {@link
 * InternalThread#current()} to get the current thread for a {@link InternalThread}.
 */
public final class InternalThreadLocalMap {

    private static final ThreadLocal<InternalThreadLocalMap> SLOW_THREAD_LOCAL = new ThreadLocal<>();

    private String requestId;

    public static InternalThreadLocalMap get() {
        Thread t = Thread.currentThread();
        if (t instanceof InternalThread) {
            return ((InternalThread) t).threadLocalMap();
        } else {
            return slowGet();
        }
    }

    private static InternalThreadLocalMap slowGet() {
        InternalThreadLocalMap ret = SLOW_THREAD_LOCAL.get();
        if (ret == null) {
            ret = new InternalThreadLocalMap();
            SLOW_THREAD_LOCAL.set(ret);
        }
        return ret;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
