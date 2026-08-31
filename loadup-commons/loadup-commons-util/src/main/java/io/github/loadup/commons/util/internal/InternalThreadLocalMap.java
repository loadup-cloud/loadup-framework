package io.github.loadup.commons.util.internal;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
