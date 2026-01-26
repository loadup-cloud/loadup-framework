package io.github.loadup.components.cache.test.common;

import io.github.loadup.components.cache.binder.CacheTicker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lise
 * @version FakeTicker.java, v 0.1 2026年01月14日 11:24 lise
 */
public class FakeTicker implements CacheTicker {
  private final AtomicLong nanos = new AtomicLong();

  public void advance(long duration, TimeUnit unit) {
    nanos.addAndGet(unit.toNanos(duration));
  }

  @Override
  public long read() {
    return nanos.get();
  }
}
