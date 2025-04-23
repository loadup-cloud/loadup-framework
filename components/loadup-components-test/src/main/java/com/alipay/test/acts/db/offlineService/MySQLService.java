/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.db.offlineService;

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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * MySQL服务
 * 
 * @author baishuo.lp
 * @version $Id: MySQLService.java, v 0.1 2015年8月13日 下午9:42:50 baishuo.lp Exp $
 */
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
                    ActsLogUtil.fail(logger, "切换schema报错" + this.schema, e);
                }
            }
        } catch (Exception e) {
            ActsLogUtil.fail(logger, "OB启动异常", e);
        }
    }

}
