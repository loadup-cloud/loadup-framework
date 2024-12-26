package com.github.loadup.components.gateway.plugin.helper.util;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 *
 */
public class DateAdapter extends TypeAdapter<Date> {

    /**
     * adpator read
     *
     * @throws IOException
     */
    @Override
    public Date read(JsonReader in) throws IOException {
        String jsonStr = in.nextString();
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        } else {
            try {
                return ISO8601Utils.parse(jsonStr, new ParsePosition(0));
            } catch (ParseException e) {
                throw new JsonSyntaxException(jsonStr, e);
            }
        }
    }

    /**
     * adpator write
     *
     * @throws IOException
     */
    @Override
    public synchronized void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        String dateFormatAsString = ISO8601Utils.format(value);
        out.value(dateFormatAsString);
    }
}