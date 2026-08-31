package io.github.loadup.commons.util.json;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
