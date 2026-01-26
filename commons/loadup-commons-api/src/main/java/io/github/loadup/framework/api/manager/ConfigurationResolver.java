package io.github.loadup.framework.api.manager;

import java.time.Duration;
import org.springframework.util.StringUtils;

/** 配置解析器：实现 Binding > Binder > Fallback 的覆盖逻辑 */
public class ConfigurationResolver {

  /**
   * 通用的优先级选择逻辑
   *
   * @param bindingVal 业务层配置（最高优先级）
   * @param binderVal 组件层配置（次高优先级）
   * @param fallback 兜底值（最低优先级）
   */
  public static <T> T resolve(T bindingVal, T binderVal, T fallback) {
    if (isValid(bindingVal)) {
      return bindingVal;
    }
    if (isValid(binderVal)) {
      return binderVal;
    }
    return fallback;
  }

  /** 两级覆盖简化版 */
  public static <T> T resolve(T bindingVal, T binderVal) {
    return isValid(bindingVal) ? bindingVal : binderVal;
  }

  /** 判定配置项是否“有效”（非空且非零） */
  private static boolean isValid(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof String) {
      return StringUtils.hasText((String) value);
    }
    if (value instanceof Number) {
      // 如果是数字且为 0，视为未配置（除非业务需要 0 也是有效值，此处按未配置处理）
      return ((Number) value).longValue() != 0;
    }
    if (value instanceof Duration) {
      return !((Duration) value).isZero();
    }
    return true;
  }
}
