package io.github.loadup.components.database.autoconfigure;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import javax.sql.DataSource;
import org.apache.ibatis.session.LocalCacheScope;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;

/**
 * Wires MyBatis-Flex for framework persistence.
 *
 * <p>Generated mappers live in the framework modules outside the integrating application's
 * auto-configuration packages, so this configuration scans the framework root package for
 * {@link BaseMapper} subtypes. The customizer applies the shared persistence conventions: the
 * {@code deleted} column is the logical-delete flag with {@code false}/{@code true} values, and
 * paging defaults follow the framework contract.
 *
 * <p>Local cache scope is {@code STATEMENT} because gateways mix mapper reads with explicit JDBC
 * writes; a session-level cache would return stale rows after an explicit update in the same
 * transaction.
 */
@AutoConfiguration(afterName = "com.mybatisflex.spring.boot.v4.MybatisFlexAutoConfiguration")
@ConditionalOnClass({MyBatisFlexCustomizer.class, BaseMapper.class, MapperScan.class})
@ConditionalOnSingleCandidate(DataSource.class)
@MapperScan(basePackages = "io.github.loadup", markerInterface = BaseMapper.class, lazyInitialization = "true")
public class MyBatisFlexAutoConfiguration {

    @Bean
    public MyBatisFlexCustomizer loadUpMyBatisFlexCustomizer() {
        return config -> {
            config.setPrintBanner(false);
            config.setLogicDeleteColumn("deleted");
            config.setNormalValueOfLogicDelete(false);
            config.setDeletedValueOfLogicDelete(true);
            config.setDefaultPageSize(20);
            config.setDefaultMaxPageSize(100);
        };
    }

    @Bean
    public ConfigurationCustomizer loadUpMyBatisConfigurationCustomizer() {
        return configuration -> configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
    }
}
