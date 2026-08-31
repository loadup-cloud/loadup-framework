package io.github.loadup.modules.upms;

/*-
 * #%L
 * Loadup UPMS Test
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

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test Application for UPMS Module Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootApplication
// @Import(MyBatisTestConfig.class)
@ComponentScan(
        basePackages = {
            "io.github.loadup.modules.upms.infrastructure.repository",
            "io.github.loadup.modules.upms.infrastructure.converter",
            "io.github.loadup.components.database"
        })
@MapperScan("io.github.loadup.modules.upms.infrastructure.mapper")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
