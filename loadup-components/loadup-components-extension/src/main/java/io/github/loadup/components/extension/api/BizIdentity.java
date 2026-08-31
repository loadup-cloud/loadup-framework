package io.github.loadup.components.extension.api;

/*-
 * #%L
 * Loadup Components Extension
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
 * 业务身份标记接口。
 *
 * <p>Command / Query 对象实现此接口后，{@code BizScenarioInterceptor} 可自动从请求参数中提取
 * {@code bizCode}，无需在 Service 方法中手动调用 {@code BizContextHolder.set()}。
 *
 * <p>示例：
 *
 * <pre>{@code
 * public class OrderCreateCommand implements BizIdentity {
 *     private String bizCode;   // e.g. "retail" / "catering"
 *     private String useCase;   // optional, e.g. "groupBuy"
 *     // ...other fields
 * }
 * }</pre>
 */
public interface BizIdentity {

    /**
     * 业务代码，对应 {@link io.github.loadup.components.extension.core.BizScenario#getBizCode()}。
     *
     * @return bizCode，不允许为 null
     */
    String getBizCode();

    /**
     * 用例标识，默认 "default"。
     *
     * @return useCase
     */
    default String getUseCase() {
        return "default";
    }

    /**
     * 场景标识，默认 "default"。
     *
     * @return scenario
     */
    default String getScenario() {
        return "default";
    }
}
