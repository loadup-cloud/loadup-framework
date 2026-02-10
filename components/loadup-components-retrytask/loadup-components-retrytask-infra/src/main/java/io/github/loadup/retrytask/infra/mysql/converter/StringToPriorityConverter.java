package io.github.loadup.retrytask.infra.mysql.converter;

import io.github.loadup.retrytask.facade.model.Priority;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.stream.Stream;

/**
 * Converts a String from the database to a Priority enum.
 */
@ReadingConverter
public class StringToPriorityConverter implements Converter<String, Priority> {

    @Override
    public Priority convert(String source) {
        return Stream.of(Priority.values())
                .filter(p -> p.getCode().equals(source))
                .findFirst()
                .orElse(Priority.LOW); // Default to LOW if not found or null
    }
}
