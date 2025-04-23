/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.constant;

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
 * 以下字段可以配置在sofa-test-config.properties，可以起到注释所描述的作用
 * 
 * @author baishuo.lp
 * @version $Id: ActsConfigConstants.java, v 0.1 2015年8月28日 下午5:10:28 baishuo.lp Exp $
 */
public class ActsConfigConstants {

    /** 是否需要跳过比较数据库列长和所得数据实际列长，默认比较*/
    public final static String SKIP_COMPARE_DB_LENGTH_KEY = "acts_skip_db_length_compare";

    /** 用于在字符串校验时过滤的字符串，用于过滤大使馆模式的不同系统名*/
    public final static String IGNORE_STRING_LIST_KEY     = "acts_string_compare_ignore_list";

    /** 用于确定是否使用单一yaml文件还是多个yaml文件（每个yaml文件为用例名）*/
    public final static String IS_SINGLE_YAML             = "acts_is_single_yaml";
}
