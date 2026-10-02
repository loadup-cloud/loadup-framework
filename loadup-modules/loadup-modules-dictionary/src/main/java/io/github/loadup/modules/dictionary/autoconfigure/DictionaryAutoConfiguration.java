package io.github.loadup.modules.dictionary.autoconfigure;

import io.github.loadup.modules.dictionary.DictionaryRepository;
import io.github.loadup.modules.dictionary.DictionaryService;
import io.github.loadup.modules.dictionary.jdbc.JdbcDictionaryRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** Wires the dictionary service to its JDBC store. */
@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.dictionary", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DictionaryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DictionaryRepository.class)
    public DictionaryRepository dictionaryRepository(DataSource dataSource) {
        return new JdbcDictionaryRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean(DictionaryService.class)
    public DictionaryService dictionaryService(DictionaryRepository repository) {
        return new DictionaryService(repository);
    }
}
