/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data.db;

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

/**
 * 
 * @author zhiyuan.lzy
 * @version $Id: ActsDbInfo.java, v 0.1 2015年10月28日 下午3:30:39 zhiyuan.lzy Exp $
 */
public class ActsDbInfo {

    protected static final String url      = "jdbc:mysql://10.244.15.3:2828";

    protected static final String userName = "dev";

    protected static final String password = "dev#$1107";

    protected static final String schema   = null;

    /**
     * Getter method for property <tt>url</tt>.
     * 
     * @return property value of url
     */
    public static String getUrl() {
        return url;
    }

    /**
     * Getter method for property <tt>username</tt>.
     * 
     * @return property value of userName
     */
    public static String getUsername() {
        return userName;
    }

    /**
     * Getter method for property <tt>password</tt>.
     * 
     * @return property value of password
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Getter method for property <tt>schema</tt>.
     * 
     * @return property value of schema
     */
    public static String getSchema() {
        return schema;
    }

}
