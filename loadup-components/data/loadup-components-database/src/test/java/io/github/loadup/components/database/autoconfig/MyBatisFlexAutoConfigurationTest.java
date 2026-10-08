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
