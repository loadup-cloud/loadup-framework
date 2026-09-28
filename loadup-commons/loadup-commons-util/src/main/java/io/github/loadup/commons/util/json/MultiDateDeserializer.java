package io.github.loadup.commons.util.json;

import io.github.loadup.commons.constant.CommonConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class MultiDateDeserializer extends StdDeserializer<Date> {
    private static final String[] DATE_FORMATS = {
        CommonConstants.DEFAULT_DATE_TIME_FORMAT, "yyyy/MM/dd", CommonConstants.DEFAULT_DATE_FORMAT
    };

    public MultiDateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String dateStr = p.getString().trim();
        for (String pattern : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                dateFormat.setLenient(false);
                return dateFormat.parse(dateStr);
            } catch (ParseException ignored) {
                // Try the next supported format.
            }
        }
        throw ctxt.weirdStringException(dateStr, Date.class, "Unsupported date format");
    }
}
