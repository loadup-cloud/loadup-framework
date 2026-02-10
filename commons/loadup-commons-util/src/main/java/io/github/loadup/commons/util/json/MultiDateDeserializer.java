package io.github.loadup.commons.util.json;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.loadup.commons.constant.CommonConstants;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultiDateDeserializer extends StdDeserializer<Date> {
    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat(CommonConstants.DEFAULT_DATE_TIME_FORMAT),
        new SimpleDateFormat("yyyy/MM/dd"),
        new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT)
    };

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
