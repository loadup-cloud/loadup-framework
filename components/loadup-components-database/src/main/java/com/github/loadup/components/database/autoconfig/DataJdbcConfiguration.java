/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.dataobject.BaseDO;
import com.github.loadup.commons.util.RandomUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.*;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.util.Arrays;

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories(basePackages = "com.github.loadup")
public class DataJdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    BeforeConvertCallback<BaseDO> beforeSaveCallback() {
        return (minion) -> {
            if (minion.getId() == null) {
                minion.setId(RandomUtil.randomString(20));
            }
            return minion;
        };
    }

    //@Bean
    //@Override
    //public JdbcCustomConversions jdbcCustomConversions() {
    //    return new JdbcCustomConversions(Arrays.asList(
    //            new ObjectToBooleanConverter(),
    //            new BooleanToObjectConverter()
    //    ));
    //}
    //
    //@ReadingConverter
    //static class ObjectToBooleanConverter implements Converter<Object, Boolean> {
    //    @Override
    //    public Boolean convert(Object source) {
    //        if (source == null) {return false;}
    //        String strVal = source.toString().toUpperCase();
    //        return "T".equals(strVal) || "Y".equals(strVal) || "1".equals(strVal);
    //    }
    //}
    //
    //@WritingConverter
    //static class BooleanToObjectConverter implements Converter<Boolean, String> {
    //    @Override
    //    public String convert(Boolean source) {
    //        return source != null && source ? "T" : "F";
    //    }
    //}
}
