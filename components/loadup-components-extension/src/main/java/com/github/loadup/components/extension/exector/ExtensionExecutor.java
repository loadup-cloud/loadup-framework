package com.github.loadup.components.extension.exector;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;
import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.register.ExtensionCoordinate;
import com.github.loadup.components.extension.register.ExtensionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtensionExecutor {

    private final ExtensionRegistry extensionRegistry;

    public ExtensionExecutor(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    /**
     * 执行有返回值的扩展点方法
     *
     * @param <E>                 扩展点接口类型
     * @param <R>                 返回值类型
     * @param extensionPointClass 扩展点接口的Class
     * @param scenario            业务场景
     * @param action              要执行的业务逻辑
     * @return 业务逻辑的返回值
     */
    public <E extends IExtensionPoint, R> R execute(Class<E> extensionPointClass, BizScenario scenario, Function<E, R> action) {
        E extension = findBestMatch(extensionPointClass, scenario);
        return action.apply(extension);
    }

    /**
     * 执行无返回值的扩展点方法
     */
    public <E extends IExtensionPoint> void executeVoid(Class<E> extensionPointClass, BizScenario scenario, Consumer<E> action) {
        E extension = findBestMatch(extensionPointClass, scenario);
        action.accept(extension);
    }

    /**
     * 执行所有匹配的扩展点（类似责任链/观察者模式）
     */
    public <E extends IExtensionPoint> void executeAll(Class<E> extensionPointClass, BizScenario scenario, Consumer<E> action) {
        List<E> extensions = findAllMatches(extensionPointClass, scenario);
        extensions.forEach(action);
    }

    @SuppressWarnings("unchecked")
    private <E extends IExtensionPoint> E findBestMatch(Class<E> extensionPointClass, BizScenario scenario) {
        List<ExtensionCoordinate> candidates = extensionRegistry.getExtensionCoordinates(extensionPointClass);

        if (CollectionUtils.isEmpty(candidates)) {
            throw new ExtensionNotFoundException("No extension found for interface: " + extensionPointClass.getName());
        }

        // 1. 精确匹配：bizCode + useCase + scenario
        Optional<ExtensionCoordinate> bestMatch = findMatch(candidates, scenario, true, true);

        // 2. 降级匹配：bizCode + useCase (忽略 scenario)
        if (bestMatch.isEmpty()) {
            bestMatch = findMatch(candidates, scenario, true, false);
        }

        // 3. 再次降级匹配：bizCode (忽略 useCase 和 scenario)
        if (bestMatch.isEmpty()) {
            bestMatch = findMatch(candidates, scenario, false, false);
        }

        // 4. 最终降级：查找 useCase 和 scenario 均为 "default" 的通用实现
        if (bestMatch.isEmpty()) {
            bestMatch = findDefaultMatch(candidates, scenario.getBizCode());
        }

        return (E) bestMatch.orElseThrow(() -> new ExtensionNotFoundException(
                "No suitable extension found for " + extensionPointClass.getSimpleName() + " with scenario: " + scenario
        )).extensionInstance();
    }

    @SuppressWarnings("unchecked")
    private <E extends IExtensionPoint> List<E> findAllMatches(Class<E> extensionPointClass, BizScenario scenario) {
        List<ExtensionCoordinate> candidates = extensionRegistry.getExtensionCoordinates(extensionPointClass);
        if (CollectionUtils.isEmpty(candidates)) {
            return new ArrayList<>();
        }

        return candidates.stream()
                .filter(c -> match(c.extensionMetadata(), scenario, true, true)) // 可以根据需求定义匹配逻辑
                .sorted(Comparator.comparingInt(c -> c.extensionMetadata().priority()))
                .map(c -> (E) c.extensionInstance())
                .collect(Collectors.toList());
    }

    private Optional<ExtensionCoordinate> findMatch(List<ExtensionCoordinate> candidates, BizScenario scenario,
                                                    boolean matchUseCase, boolean matchScenario) {
        return candidates.stream()
                .filter(c -> match(c.extensionMetadata(), scenario, matchUseCase, matchScenario))
                .min(Comparator.comparingInt(c -> c.extensionMetadata().priority()));
    }

    private Optional<ExtensionCoordinate> findDefaultMatch(List<ExtensionCoordinate> candidates, String bizCode) {
        return candidates.stream()
                .filter(c -> c.extensionMetadata().bizCode().equals(bizCode) &&
                        c.extensionMetadata().useCase().equals("default") &&
                        c.extensionMetadata().scenario().equals("default"))
                .min(Comparator.comparingInt(c -> c.extensionMetadata().priority()));
    }

    private boolean match(Extension meta, BizScenario scenario, boolean matchUseCase, boolean matchScenario) {
        if (!Objects.equals(meta.bizCode(), scenario.getBizCode())) {
            return false;
        }
        if (matchUseCase && !Objects.equals(meta.useCase(), scenario.getUseCase())) {
            return false;
        }
        return !matchScenario || Objects.equals(meta.scenario(), scenario.getScenario());
    }

    // 自定义异常
    public static class ExtensionNotFoundException extends RuntimeException {
        public ExtensionNotFoundException(String message) {
            super(message);
        }
    }
}