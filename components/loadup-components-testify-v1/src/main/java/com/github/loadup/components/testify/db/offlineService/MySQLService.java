/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.db.offlineService;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.components.testify.db.enums.DataBaseTypeEnum;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * MySQL服务
 *
 *
 *
 */
@Slf4j
public class MySQLService extends AbstractDBService {

    public MySQLService(String url, String userName, String password, String schema) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.schema = schema;
        this.dbType = DataBaseTypeEnum.MYSQL;
        initConnection();
    }

    @Override
    public void initConnection() {
        try {
            if (conn == null) {
                conn = DriverManager.getConnection(url, userName, password);
            } else if (this.isConnClosed()) {
                conn = DriverManager.getConnection(url, userName, password);
            }
            if (StringUtils.isNotBlank(this.schema)) {
                String sql = "use " + this.schema.trim();
                try {
                    Statement stamt = this.conn.createStatement();
                    stamt.execute(sql);
                    stamt.close();
                } catch (SQLException e) {
                    TestifyLogUtil.fail(log, "切换schema报错" + this.schema, e);
                }
            }
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "OB启动异常", e);
        }
    }
}
