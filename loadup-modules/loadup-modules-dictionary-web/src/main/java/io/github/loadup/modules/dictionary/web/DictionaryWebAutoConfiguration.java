package io.github.loadup.modules.dictionary.web;

import io.github.loadup.modules.dictionary.DictionaryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** Optional Spring MVC surface for the data dictionary. */
@AutoConfiguration(afterName = "io.github.loadup.modules.dictionary.autoconfigure.DictionaryAutoConfiguration")
@ConditionalOnBean(DictionaryService.class)
@ConditionalOnProperty(prefix = "loadup.dictionary.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DictionaryWebAutoConfiguration {
    @Bean
    public DictionaryController dictionaryController(DictionaryService service) {
        return new DictionaryController(service);
    }
}
