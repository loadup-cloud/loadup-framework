package io.github.loadup.retrytask.infra.mysql.converter;

import io.github.loadup.retrytask.facade.model.Priority;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 * Converts a Priority enum to its String representation for database storage.
 */
@WritingConverter
public class PriorityToStringConverter implements Converter<Priority, String> {

    @Override
    public String convert(Priority source) {
        return source.getCode();
    }
}
