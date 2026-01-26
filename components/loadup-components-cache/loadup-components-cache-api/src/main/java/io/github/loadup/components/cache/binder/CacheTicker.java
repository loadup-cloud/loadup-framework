
package io.github.loadup.components.cache.binder;

/**
 * @author lise
 * @version CacheTicker.java, v 0.1 2026年01月14日 11:22 lise
 */
public interface CacheTicker {
    // 返回当前纳秒值
    long read();

    // 默认实现：使用系统时间
    CacheTicker SYSTEM = System::nanoTime;
}
