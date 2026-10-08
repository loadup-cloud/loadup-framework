package io.github.loadup.components.database.id;

import com.mybatisflex.core.keygen.IKeyGenerator;
import io.github.loadup.components.database.config.DatabaseProperties;
import java.util.Objects;

/** Adapts the configured LoadUp ID strategy to MyBatis-Flex. */
public final class DatabaseIdGenerator implements IdGenerator, IKeyGenerator {
    public static final String KEY = "loadupId";

    private final IdGenerator delegate;

    public DatabaseIdGenerator(DatabaseProperties.IdGenerator properties) {
        this.delegate = createDelegate(Objects.requireNonNull(properties));
    }

    @Override
    public String generate() {
        return delegate.generate();
    }

    @Override
    public Object generate(Object entity, String keyColumn) {
        return generate();
    }

    private static IdGenerator createDelegate(DatabaseProperties.IdGenerator properties) {
        return switch (properties.getStrategy()) {
            case RANDOM -> new RandomIdGenerator(properties.getRandomLength());
            case UUID_V4 -> new UuidV4IdGenerator(properties.isUuidWithHyphens());
            case UUID_V7 -> new UuidV7IdGenerator(properties.isUuidWithHyphens());
            case SNOWFLAKE ->
                new SnowflakeIdGenerator(properties.getSnowflakeWorkerId(), properties.getSnowflakeDatacenterId());
        };
    }
}
