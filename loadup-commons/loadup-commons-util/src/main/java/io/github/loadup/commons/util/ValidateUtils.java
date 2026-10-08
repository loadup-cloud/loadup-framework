package io.github.loadup.commons.util;

import io.github.loadup.commons.log.LogUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jakarta Bean Validation 工具类
 *
 * <p>提供基于 JSR-380 规范的对象验证功能，支持：
 *
 * <ul>
 *   <li>对象验证
 *   <li>验证结果检查
 *   <li>错误消息提取
 *   <li>验证异常处理
 * </ul>
 *
 * @author loadup_cloud
 * @since 1.0.0
 */
public class ValidateUtils {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    /**
     * 验证对象，如果验证失败则抛出异常
     *
     * @param obj 待验证对象
     * @throws IllegalArgumentException 如果对象为null或验证失败
     */
    public static void validate(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);

        if (!violations.isEmpty()) {
            String errorMsg = formatViolations(violations);
            LogUtil.warn(
                    ValidateUtils.class,
                    "Validation failed for {}: {}",
                    obj.getClass().getSimpleName(),
                    errorMsg);
            throw new ValidationException(errorMsg);
        }
    }

    /**
     * 验证对象，返回是否验证通过
     *
     * @param obj 待验证对象
     * @return true-验证通过，false-验证失败或对象为null
     */
    public static boolean isValid(Object obj) {
        if (obj == null) {
            LogUtil.debug(ValidateUtils.class, "Object to validate is null, returning false");
            return false;
        }

        try {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(obj);
            return violations.isEmpty();
        } catch (Exception e) {
            LogUtil.error(
                    ValidateUtils.class,
                    "Validation error for {}",
                    obj.getClass().getSimpleName(),
                    e);
            return false;
        }
    }

    /**
     * 获取对象的所有验证错误
     *
     * @param obj 待验证对象
     * @param <T> 对象类型
     * @return 验证错误集合，如果验证通过则返回空集合
     */
    public static <T> Set<ConstraintViolation<T>> getViolations(T obj) {
        if (obj == null) {
            LogUtil.debug(ValidateUtils.class, "Object to validate is null, returning empty set");
            return Set.of();
        }

        Validator validator = factory.getValidator();
        return validator.validate(obj);
    }

    /**
     * 获取验证错误消息
     *
     * @param violation 约束违反对象
     * @return 错误消息
     */
    public static String getErrorMessage(ConstraintViolation<?> violation) {
        if (violation == null) {
            return "";
        }
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    /**
     * 格式化所有验证错误为单个字符串
     *
     * @param violations 验证错误集合
     * @return 格式化的错误消息
     */
    public static String formatViolations(Set<? extends ConstraintViolation<?>> violations) {
        if (violations == null || violations.isEmpty()) {
            return "";
        }

        return violations.stream().map(ValidateUtils::getErrorMessage).collect(Collectors.joining("; "));
    }

    /**
     * 获取 Validator 实例
     *
     * @return Validator 实例
     */
    public static Validator getValidator() {
        return factory.getValidator();
    }
}
