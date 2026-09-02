/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

package io.github.loadup.components.database.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.id.DatabaseIdGenerator;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class MyBatisFlexAutoConfigurationTest {
    @Test
    void delegatesFlexKeyGenerationToCustomBean() {
        DatabaseProperties properties = new DatabaseProperties();
        MyBatisFlexAutoConfiguration autoConfiguration = new MyBatisFlexAutoConfiguration(properties);
        FlexGlobalConfig globalConfig = new FlexGlobalConfig();

        autoConfiguration
                .myBatisFlexCustomizer(() -> "custom-id", Clock.systemUTC())
                .customize(globalConfig);

        assertThat(globalConfig.getKeyConfig().getKeyType()).isEqualTo(KeyType.Generator);
        assertThat(globalConfig.getKeyConfig().getValue()).isEqualTo(DatabaseIdGenerator.KEY);
        assertThat(KeyGeneratorFactory.getKeyGenerator(DatabaseIdGenerator.KEY).generate(new Object(), "id"))
                .isEqualTo("custom-id");
    }
}
