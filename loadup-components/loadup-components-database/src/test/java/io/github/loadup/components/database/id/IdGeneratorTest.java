package io.github.loadup.components.database.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.github.loadup.components.database.config.DatabaseProperties;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {
    @Test
    void generatesConfiguredStrategies() {
        for (DatabaseProperties.Strategy strategy : DatabaseProperties.Strategy.values()) {
            DatabaseProperties.IdGenerator properties = new DatabaseProperties.IdGenerator();
            properties.setStrategy(strategy);
            String id = new DatabaseIdGenerator(properties).generate();

            assertThat(id).isNotBlank().hasSizeBetween(1, 64);
            if (strategy == DatabaseProperties.Strategy.UUID_V4) {
                assertThat(id).hasSize(32).doesNotContain("-");
            }
            if (strategy == DatabaseProperties.Strategy.UUID_V7) {
                assertThat(id).hasSize(32);
                assertThat(id.charAt(12)).isEqualTo('7');
            }
        }
    }

    @Test
    void validatesRandomLength() {
        assertThatIllegalArgumentException().isThrownBy(() -> new RandomIdGenerator(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new RandomIdGenerator(65));
    }

    @Test
    void validatesSnowflakeWorkerAndDatacenterIds() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(-1, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new SnowflakeIdGenerator(0, 32));
        assertThat(new SnowflakeIdGenerator(1, 2).generate()).matches("\\d+");
    }
}
