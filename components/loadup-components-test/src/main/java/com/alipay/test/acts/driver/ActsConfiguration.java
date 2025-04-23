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
import com.alipay.test.acts.constant.ActsConstants;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * Acts-config.properties
 * 
 * @author mokong
 * @version $Id: ActsConfiguration.java, v 0.1 2015-10-8 root Exp $
 */
public class ActsConfiguration {

    private static final Log                  LOG           = LogFactory
                                                                .getLog(ActsConfiguration.class);

    private static Map<String, String>        actsConfigMap = new HashMap<String, String>();

    private Properties                        dbProperties  = new Properties();

    private String                            DB_Mode       = "devdb";

    private String                            testDataSourceFile;

    /** TRģʽ*/
    private boolean                           isTRMode      = false;

    /** 关键日志输出带上颜色 */
    private static boolean                    coloredLog    = false;

    private static volatile ActsConfiguration instance      = null;

    private ActsConfiguration() {

    }

    public static ActsConfiguration getInstance() {
        if (instance == null) {
            synchronized (ActsConfiguration.class) {
                if (instance == null) {
                    instance = new ActsConfiguration();
                    loadActsProperties();
                }

            }
        }

        return instance;
    }

    public static void loadActsProperties() {

//        if (actsConfigMap.isEmpty()) {
//
//            Properties ActsProperties = getProperties(ActsConstants.ACTS_CONFIG_BASE_DIR
//                                                      + ActsConstants.ACTS_CONFIG_FILE_NAME);
//
//            Set<Map.Entry<Object, Object>> entrySet = ActsProperties.entrySet();
//            for (Map.Entry<Object, Object> entry : entrySet) {
//                Object keyObject = entry.getKey();
//                Object valueObject = entry.getValue();
//                String envConfigValue = System.getProperty(ActsConstants.ACTS_CONFIG_SYSENV_PREFIX
//                                                           + keyObject.toString());
//                if (StringUtils.isNotBlank(envConfigValue)) {
//                    actsConfigMap.put(keyObject.toString(), envConfigValue);
//                } else {
//                    actsConfigMap.put(keyObject.toString(), valueObject.toString());
//                }
//            }
//        }

        readJvmArgs();
    }

    public static String getActsProperty(String key) {
        loadActsProperties();
        return actsConfigMap.get(key);
    }

    public boolean isOffLine() {
        return actsConfigMap.isEmpty();
    }

    public void loadDBConfiguration() {
        if (actsConfigMap.isEmpty()) {
            loadActsProperties();
            String dbconfFile = actsConfigMap.get(ActsConstants.DB_CONF_KEY);

            if (StringUtils.isNotBlank(dbconfFile)) {
                DB_Mode = getDbMode(dbconfFile);

                dbconfFile = ActsConstants.DB_CONF_DIR + dbconfFile.trim();
                dbProperties = getProperties(dbconfFile);
            } else {
                dbconfFile = ActsConstants.ACTS_CONFIG_BASE_DIR
                             + ActsConstants.ACTS_CONFIG_FILE_NAME;
                ActsLogUtil.warn(LOG, "ACTS配置项没有 [dbconf_file]！");
            }

            testDataSourceFile = ActsConstants.TEST_DATA_SOURCE_DIR + DB_Mode
                                 + "-test-datasource.xml";
            isTRMode = "true".equalsIgnoreCase(StringUtils.trim(actsConfigMap
                .get(ActsConstants.TR_MODE)));
        }

    }

    public String getActsDBProperty(String key) {
        loadDBConfiguration();
        return (String) dbProperties.get(key);
    }

    /**
     * 读取jvm相关参数变量
     */
    private static void readJvmArgs() {
        coloredLog = Boolean.valueOf(System.getProperty("coloredLog", "false"));
    }

    public String getSofaConfig(String keyName) {
        String sofaConfig = "";//"".getSofaConfig(keyName);
        String envSofaValue = System.getProperty(ActsConstants.SOFA_CONFIG_SYSENV_PREFIX
                                                 + keyName.toString());
        if (StringUtils.isNotBlank(envSofaValue)) {
            return envSofaValue;
        } else {
            return sofaConfig;
        }
    }

    public String[] getTestConfigPattern() {
        loadActsProperties();
        String configPattern = DB_Mode + "-sofa-test-config.properties";
        String sofaTestMode = System.getProperty("sofatest.acts.db.mode", "");
        if (!StringUtils.isEmpty(sofaTestMode)) {
            configPattern = sofaTestMode + "-sofa-test-config.properties";
        }
        ActsLogUtil.info(LOG, "ACTS配置初始化：sofaTestConfigFile =  "
                              + ActsConstants.SOFA_TEST_CONFIG_DIR + configPattern);
        return new String[] { ActsConstants.SOFA_TEST_CONFIG_DIR, configPattern };
    }

    private static Properties getProperties(String propertiesPath) {
        Properties properties = new Properties();
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            URL ActsConfigUrl = currentClassLoader.getResource(propertiesPath);
            properties.load(ActsConfigUrl.openStream());
        } catch (Exception e) {
            ActsLogUtil.fail(LOG, "加载配置文件出错" + propertiesPath, e);
        }
        return properties;
    }

    private String getDbMode(String dbconfFile) {
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

    public Map<String, String> getActsConfigMap() {
        return actsConfigMap;
    }

    public boolean isColoredLog() {
        return coloredLog;
    }

}
