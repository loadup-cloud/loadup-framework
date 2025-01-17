package com.github.loadup.components.retrytask.config;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.components.retrytask.constant.RetryTaskConstants;

import java.util.Map;
import javax.sql.DataSource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * the config of retry task datasource
 */
@Getter
@Setter
@Component
@NoArgsConstructor
public class RetryDataSourceConfig {

    /**
     * business type, users can define themselves
     * <p>
     * DEFAULT(in default)
     */
    private String bizType = RetryTaskConstants.DEFAULT_BIZ_TYPE;
    /**
     * the mode of the relation of business tables and task tables
     * <p>
     * SAME(in default, business tables and task tables are in the same database)
     * DIFFERENT(business tables and task tables are in the different database)
     */
    private String dbMode = "SAME";
    /**
     * datasource
     */
    private DataSource dataSource;
    /**
     * the prefix of table name
     */
    private String tablePrefix;
    /**
     * sql sentence in every database
     * <p>
     * key:  DbType-SqlType
     * value: sql sentence
     */
    private Map<String, String> sqlMap;

    private String dbType = "MYSQL";

    public RetryDataSourceConfig(String bizType) {
        this.bizType = bizType;
    }

}
