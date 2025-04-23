/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.driver;

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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.driver.constants.AtsConstants;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * 用于存储ATS静态信息
 * 
 * @author baishuo.lp
 * @version $Id: AtsContext.java, v 0.1 2015年8月16日 下午9:13:31 baishuo.lp Exp $
 */
public class AtsConfiguration {

    private static final Log          LOG          = LogFactory.getLog(AtsConfiguration.class);

    /** ATS属性配置Map */
    public static Map<String, String> atsConfigMap = new HashMap<String, String>();

    /** ATS默认DB模式 */
    public static String              DB_Mode      = "devdb";

    /** ATS当前DB模式加载属性 */
    public static Properties          dbProperties = new Properties();

    /** 测试数据源文件 */
    public static String              testDataSourceFile;

    /** TR模式 */
    public static boolean             isTRMode     = false;

    /**
     * 从默认路径加载ATS配置文件
     * 
     * @return
     */
    public static void loadAtsProperties() {
        if (atsConfigMap.isEmpty()) {
            Properties atsProperties = getProperties(AtsConstants.ATS_CONFIG_BASE_DIR
                                                     + AtsConstants.ATS_CONFIG_FILE_NAME);
            Set<Map.Entry<Object, Object>> entrySet = atsProperties.entrySet();
            for (Map.Entry<Object, Object> entry : entrySet) {
                Object keyObject = entry.getKey();
                Object valueObject = entry.getValue();
                String envConfigValue = System.getProperty(AtsConstants.ATS_CONFIG_SYSENV_PREFIX
                                                           + keyObject.toString());
                if (StringUtils.isNotBlank(envConfigValue)) {
                    atsConfigMap.put(keyObject.toString(), envConfigValue);
                } else {
                    atsConfigMap.put(keyObject.toString(), valueObject.toString());
                }
            }
        }
    }

    /**
     * 获取Ats config配置属性
     * 
     * @param key
     * @return
     */
    public static String getAtsProperty(String key) {
        loadAtsProperties();
        return atsConfigMap.get(key);
    }

    /**
     * 判断是否为线下，以未加载ATS配置文件为准
     * 
     * @return
     */
    public static boolean isOffLine() {
        return atsConfigMap.isEmpty();
    }

    /**
     * 加载DB配置项
     */
    public static void loadDBConfiguration() {
        if (atsConfigMap.isEmpty()) {
            loadAtsProperties();
            // 可配置数据源文件参数(自由度很高)
            String dbconfFile = atsConfigMap.get(AtsConstants.DB_CONF_KEY);

            if (StringUtils.isNotBlank(dbconfFile)) {
                // 获取数据库环境模式标记（DB_MODE）
                DB_Mode = getDbMode(dbconfFile);

                dbconfFile = AtsConstants.DB_CONF_DIR + dbconfFile.trim();
                dbProperties = getProperties(dbconfFile);
            } else {
                dbconfFile = AtsConstants.ATS_CONFIG_BASE_DIR + AtsConstants.ATS_CONFIG_FILE_NAME;
                ActsLogUtil.warn(LOG, "ACTS配置项没有 [dbconf_file]！");
            }

            testDataSourceFile = AtsConstants.TEST_DATA_SOURCE_DIR + DB_Mode
                                 + "-test-datasource.xml";
            isTRMode = "true".equalsIgnoreCase(StringUtils.trim(atsConfigMap
                .get(AtsConstants.TR_MODE)));
        }
    }

    /**
     * 获取Ats db config配置属性
     * 
     * @param key
     * @return
     */
    public static String getAtsDBProperty(String key) {
        loadDBConfiguration();
        return (String) dbProperties.get(key);
    }

    /**
     * 获取Sofa配置项
     */
    public static String getSofaConfig(String keyName) {
        String sofaConfig ="";// TestUtils.getSofaConfig(keyName);
        String envSofaValue = System.getProperty(AtsConstants.SOFA_CONFIG_SYSENV_PREFIX
                                                 + keyName.toString());
        if (StringUtils.isNotBlank(envSofaValue)) {
            return envSofaValue;
        } else {
            return sofaConfig;
        }
    }

    /**
     * 根据数据库环境模式标记（DB_MODE），定位sofa-test-config.properties文件匹配规则
     * 
     * @return
     */
    public static String[] getTestConfigPattern() {

        loadAtsProperties();

        String configPattern = DB_Mode + "-sofa-test-config.properties";
        // 用系统变量再做一次替换,系统变量可以覆盖默认变量，方便hudson上通过“mvn -D参数”方式修改全局变量
        String sofaTestMode = System.getProperty("sofatest.ats.db.mode", "");
        if (!StringUtils.isEmpty(sofaTestMode)) {
            configPattern = sofaTestMode + "-sofa-test-config.properties";
        }
        ActsLogUtil.info(LOG, "ACTS配置初始化：sofaTestConfigFile = " + AtsConstants.SOFA_TEST_CONFIG_DIR
                              + configPattern);
        return new String[] { AtsConstants.SOFA_TEST_CONFIG_DIR, configPattern };
    }

    /**
     * 获取指定文件属性
     * 
     * @param propertiesPath
     * @return
     */
    private static Properties getProperties(String propertiesPath) {
        Properties properties = new Properties();
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            URL atsConfigUrl = currentClassLoader.getResource(propertiesPath);
            properties.load(atsConfigUrl.openStream());
        } catch (Exception e) {
            ActsLogUtil.fail(LOG, "加载配置文件出错 " + propertiesPath, e);
        }
        return properties;
    }

    /**
     * 根据数据源文件name取得数据库环境模式标记
     * 
     * @param dbconfFile
     * @return
     * @author mokong
     */
    private static String getDbMode(String dbconfFile) {
        String[] strs = dbconfFile.split(".conf");
        String dbMode = strs[0].trim();
        // 用系统变量再做一次替换,系统变量可以覆盖默认变量，方便hudson上通过“mvn -D参数”方式修改全局变量，用于修改
        // dbconf_file的值
        String sofaTestMode = System.getProperty("sofatest.db_mode", "");
        if (!StringUtils.isEmpty(sofaTestMode)) {
            dbMode = sofaTestMode;
        }
        ActsLogUtil.info(LOG, "ACTS配置初始化： DB_MODE = " + dbMode);
        return dbMode;
    }

}
