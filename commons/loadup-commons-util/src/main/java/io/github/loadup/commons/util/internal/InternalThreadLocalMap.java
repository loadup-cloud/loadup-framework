package io.github.loadup.commons.util.internal;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The internal data structure that stores the thread-local variables for Netty and all {@link
 * InternalThread}s. Note that this class is for internal use only. Use {@link
 * Thread#currentThread()} to get the current thread for a regular thread, and use {@link
 * InternalThread#current()} to get the current thread for a {@link InternalThread}.
 */
public final class InternalThreadLocalMap {

  private static final ThreadLocal<InternalThreadLocalMap> slowThreadLocal = new ThreadLocal<>();
  private static final AtomicInteger nextIndex = new AtomicInteger();

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
    InternalThreadLocalMap ret = slowThreadLocal.get();
    if (ret == null) {
      ret = new InternalThreadLocalMap();
      slowThreadLocal.set(ret);
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
