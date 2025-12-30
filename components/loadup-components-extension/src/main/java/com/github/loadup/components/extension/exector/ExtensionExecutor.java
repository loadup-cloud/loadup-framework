package com.github.loadup.components.extension.exector;

/*-
 * #%L
 * loadup-components-extension
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

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;
import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.register.ExtensionCoordinate;
import com.github.loadup.components.extension.register.ExtensionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 扩展点执行器
 */
@Slf4j
public class ExtensionExecutor {

    private final ExtensionRegistry extensionRegistry;

    public ExtensionExecutor(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    /**
     * 执行单个最佳匹配的扩展点（有返回值）
     * 使用降级匹配策略自动找到最合适的扩展点实现
     */
    public <E extends IExtensionPoint, R> R execute(Class<E> extensionPointClass, BizScenario scenario, Function<E, R> action) {
        E extension = findBestMatch(extensionPointClass, scenario);
        log.debug("Executing extension {} for scenario {}", extension.getClass().getSimpleName(), scenario.getUniqueIdentity());
        return action.apply(extension);
    }

    /**
     * 执行单个最佳匹配的扩展点（无返回值）
     */
    public <E extends IExtensionPoint> void run(Class<E> extensionPointClass, BizScenario scenario, Consumer<E> action) {
        E extension = findBestMatch(extensionPointClass, scenario);
        log.debug("Executing extension {} for scenario {}", extension.getClass().getSimpleName(), scenario.getUniqueIdentity());
        action.accept(extension);
    }

    /**
     * 执行指定场景的所有匹配扩展点（精确匹配）
     */
    public <E extends IExtensionPoint> void executeAll(Class<E> extensionPointClass, BizScenario scenario, Consumer<E> action) {
        List<E> extensions = findAllByScenario(extensionPointClass, scenario);
        if (extensions.isEmpty()) {
            log.warn("No extensions found for scenario: {}", scenario.getUniqueIdentity());
            return;
        }
        log.debug("Executing {} extensions for scenario {}", extensions.size(), scenario.getUniqueIdentity());
        extensions.forEach(action);
    }

    /**
     * 执行指定业务代码的所有扩展点（忽略 useCase 和 scenario）
     */
    public <E extends IExtensionPoint> void executeAll(Class<E> extensionPointClass, String bizCode, Consumer<E> action) {
        List<E> extensions = findAllByBizCode(extensionPointClass, bizCode);
        if (extensions.isEmpty()) {
            log.warn("No extensions found for bizCode: {}", bizCode);
            return;
        }
        log.debug("Executing {} extensions for bizCode {}", extensions.size(), bizCode);
        extensions.forEach(action);
    }

    /**
     * 收集指定场景所有扩展点的执行结果
     */
    public <E extends IExtensionPoint, R> List<R> collect(Class<E> extensionPointClass, BizScenario scenario, Function<E, R> action) {
        List<E> extensions = findAllByScenario(extensionPointClass, scenario);
        if (extensions.isEmpty()) {
            log.warn("No extensions found for scenario: {}", scenario.getUniqueIdentity());
            return Collections.emptyList();
        }
        log.debug("Collecting results from {} extensions for scenario {}", extensions.size(), scenario.getUniqueIdentity());
        return extensions.stream()
            .map(action)
            .collect(Collectors.toList());
    }

    /**
     * 收集指定业务代码所有扩展点的执行结果
     */
    public <E extends IExtensionPoint, R> List<R> collect(Class<E> extensionPointClass, String bizCode, Function<E, R> action) {
        List<E> extensions = findAllByBizCode(extensionPointClass, bizCode);
        if (extensions.isEmpty()) {
            log.warn("No extensions found for bizCode: {}", bizCode);
            return Collections.emptyList();
        }
        log.debug("Collecting results from {} extensions for bizCode {}", extensions.size(), bizCode);
        return extensions.stream()
            .map(action)
            .collect(Collectors.toList());
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
            "No suitable extension found for " + extensionPointClass.getSimpleName() + " with scenario: " + scenario.getUniqueIdentity()
        )).extensionInstance();
    }

    @SuppressWarnings("unchecked")
    private <E extends IExtensionPoint> List<E> findAllByScenario(Class<E> extensionPointClass, BizScenario scenario) {
        List<ExtensionCoordinate> candidates = extensionRegistry.getExtensionsByScenario(extensionPointClass, scenario);
        if (CollectionUtils.isEmpty(candidates)) {
            return Collections.emptyList();
        }

        return candidates.stream()
            .sorted(Comparator.comparingInt(c -> c.extensionMetadata().priority()))
            .map(c -> (E) c.extensionInstance())
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <E extends IExtensionPoint> List<E> findAllByBizCode(Class<E> extensionPointClass, String bizCode) {
        List<ExtensionCoordinate> allCandidates = extensionRegistry.getExtensionsByBizCode(bizCode);
        if (CollectionUtils.isEmpty(allCandidates)) {
            return Collections.emptyList();
        }

        return allCandidates.stream()
            .filter(c -> extensionPointClass.isAssignableFrom(c.extensionInstance().getClass()))
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

    public static class ExtensionNotFoundException extends RuntimeException {
        public ExtensionNotFoundException(String message) {
            super(message);
        }
    }
}

