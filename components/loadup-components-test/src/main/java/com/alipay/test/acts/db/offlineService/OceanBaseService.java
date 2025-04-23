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

import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * OceanBase服务
 * 
 * @author baishuo.lp
 * @version $Id: OceanBaseService.java, v 0.1 2015年8月13日 下午9:42:50 baishuo.lp Exp $
 */
public class OceanBaseService extends AbstractDBService {

//    private final OceanbaseDataSourceProxy oceanbaseDataSourceProxy = new OceanbaseDataSourceProxy();

    public OceanBaseService(String url) {
        this.url = url;
        initConnection();
        this.dbType = DataBaseTypeEnum.OCEANBASE;
    }

    @Override
    public void initConnection() {
        try {
            if (this.conn == null) {
//                oceanbaseDataSourceProxy.setConfigURL(url);
//                oceanbaseDataSourceProxy.init();
//                this.conn = oceanbaseDataSourceProxy.getConnection();
            } else if (this.isConnClosed()) {
//                this.conn = oceanbaseDataSourceProxy.getConnection();
            }
        } catch (Exception e) {
            ActsLogUtil.fail(logger, "OB启动异常", e);
        }
    }

}
