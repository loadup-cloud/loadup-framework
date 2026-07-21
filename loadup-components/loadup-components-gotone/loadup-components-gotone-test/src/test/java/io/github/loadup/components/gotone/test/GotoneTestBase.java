package io.github.loadup.components.gotone.test;

/*-
 * #%L
 * loadup-components-gotone-test
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

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.testify.starter.base.TestifyBase;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Gotone 测试基类
 *
 * <p>集成 Testify + TestContainers
 * <p>所有测试类继承此类即可使用 YAML 配置测试
 */
@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
public abstract class GotoneTestBase extends TestifyBase {

    // 测试基类，提供 testify 的所有能力
    // 子类可以直接使用 runTest() 方法和 val() 辅助函数
}
