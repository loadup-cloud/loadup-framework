package com.github.loadup.commons.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.loadup.commons.constant.CommonConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultiDateDeserializer extends StdDeserializer<Date> {
    private static final SimpleDateFormat[] DATE_FORMATS = {
            new SimpleDateFormat(CommonConstants.DEFAULT_DATE_TIME_FORMAT),
            new SimpleDateFormat("yyyy/MM/dd"),
            new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT)};

    public MultiDateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().trim();
        for (SimpleDateFormat dateFormat : DATE_FORMATS) {
            try {
                return dateFormat.parse(dateStr);
            } catch (ParseException ignored) {
                // 尝试下一个格式
            }
        }
        throw new IOException("Unable to parse date: " + dateStr);
    }

}
