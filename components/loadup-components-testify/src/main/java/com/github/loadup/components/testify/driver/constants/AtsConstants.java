/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.driver.constants;

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
 * ATS中沿用常量参数
 *
 *
 *
 */
public class AtsConstants {

    /**
     * ATS配置文件目录
     */
    public static final String ATS_CONFIG_BASE_DIR = "config/";

    /**
     * ATS配置文件名
     */
    public static final String ATS_CONFIG_FILE_NAME = "ats-config.properties";

    /**
     * ATS配置文件系统属性中键名
     */
    public static final String ATS_CONFIG_SYSENV_PREFIX = "ats.config.";

    /**
     * sofa配置文件系统属性中键名
     */
    public static final String SOFA_CONFIG_SYSENV_PREFIX = "sofatest.";

    /**
     * ATS配置文件中db配置项
     */
    public static final String DB_CONF_KEY = "dbconf_file";

    /**
     * ATS DB配置文件路径
     */
    public static final String DB_CONF_DIR = ATS_CONFIG_BASE_DIR + "dbConf/";

    /**
     * ATS DB数据源文件路径
     */
    public static final String TEST_DATA_SOURCE_DIR = ATS_CONFIG_BASE_DIR + "testDataSource/";

    /**
     * tr调用控制
     */
    public static final String TR_MODE = "tr_mode";

    /**
     * 只跑指定的用例集
     */
    public static final String SMOKE_TEST_FLAG = "smoke_test";

    /**
     * 指定DataSource Bean 名称,从该Bean中获取DB连接
     */
    public static final String DATA_SOURCE_BEAN_NAME = "data_source_bean_name";

    /**
     * 数据源Key开头字符
     */
    public static final String DATASOURCE_KEY = "ds";

    /**
     * 用来配置DataSource的xml文件
     */
    public static final String DATA_SOURCE_XML_FILE = "data_source_xml_file";

    /**
     * sofa-test-config.properties的目录
     */
    public static final String SOFA_TEST_CONFIG_DIR = ATS_CONFIG_BASE_DIR + "sofaTestConfig/";

    /**
     * 最小依赖bundle
     */
    public static final String MINI_BUNDLE = "mini_bundle";

    /**
     * 启动替换xml
     */
    public static final String MINI_EXCLUDE_XML = "mini_exclude_xml";
}
