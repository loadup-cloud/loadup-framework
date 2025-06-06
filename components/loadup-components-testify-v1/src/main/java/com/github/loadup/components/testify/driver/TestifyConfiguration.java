/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.driver;

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

import com.github.loadup.components.testify.constant.TestifyConstants;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 */
@Slf4j
public class TestifyConfiguration {

    private static Map<String, String> configMap = new HashMap<>();
    /**
     * 关键日志输出带上颜色
     */
    private static boolean coloredLog = false;

    private static volatile TestifyConfiguration instance = null;
    private Properties dbProperties = new Properties();
    private String DB_Mode = "devdb";
    private String testDataSourceFile;
    /**
     * TRģʽ
     */
    private boolean isTRMode = false;

    private TestifyConfiguration() {}

    public static TestifyConfiguration getInstance() {
        if (instance == null) {
            synchronized (TestifyConfiguration.class) {
                if (instance == null) {
                    instance = new TestifyConfiguration();
                    loadProperties();
                }
            }
        }

        return instance;
    }

    public static void loadProperties() {

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

    public static String getProperty(String key) {
        loadProperties();
        return configMap.get(key);
    }

    /**
     * 读取jvm相关参数变量
     */
    private static void readJvmArgs() {
        coloredLog = Boolean.valueOf(System.getProperty("coloredLog", "false"));
    }

    private static Properties getProperties(String propertiesPath) {
        Properties properties = new Properties();
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            URL configUrl = currentClassLoader.getResource(propertiesPath);
            properties.load(configUrl.openStream());
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "加载配置文件出错" + propertiesPath, e);
        }
        return properties;
    }

    public boolean isOffLine() {
        return configMap.isEmpty();
    }

    public void loadDBConfiguration() {
        if (configMap.isEmpty()) {
            loadProperties();
            String dbconfFile = configMap.get(TestifyConstants.DB_CONF_KEY);

            if (StringUtils.isNotBlank(dbconfFile)) {
                DB_Mode = getDbMode(dbconfFile);

                dbconfFile = TestifyConstants.DB_CONF_DIR + dbconfFile.trim();
                dbProperties = getProperties(dbconfFile);
            } else {
                dbconfFile = TestifyConstants.TESTIFY_CONFIG_BASE_DIR + TestifyConstants.TESTIFY_CONFIG_FILE_NAME;
                TestifyLogUtil.warn(log, "ACTS配置项没有 [dbconf_file]！");
            }

            testDataSourceFile = TestifyConstants.TEST_DATA_SOURCE_DIR + DB_Mode + "-test-datasource.xml";
            isTRMode = "true".equalsIgnoreCase(StringUtils.trim(configMap.get(TestifyConstants.TR_MODE)));
        }
    }

    public String getSofaConfig(String keyName) {
        return "";
    }

    public String[] getTestConfigPattern() {
        loadProperties();
        String configPattern = DB_Mode + "-sofa-test-config.properties";
        String sofaTestMode = System.getProperty("sofatest.acts.db.mode", "");
        if (!StringUtils.isEmpty(sofaTestMode)) {
            configPattern = sofaTestMode + "-sofa-test-config.properties";
        }
        TestifyLogUtil.info(
                log, "ACTS配置初始化：sofaTestConfigFile =  " + TestifyConstants.SOFA_TEST_CONFIG_DIR + configPattern);
        return new String[] {TestifyConstants.SOFA_TEST_CONFIG_DIR, configPattern};
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
        TestifyLogUtil.info(log, "ACTS配置初始化： DB_MODE = " + dbMode);
        return dbMode;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public boolean isColoredLog() {
        return coloredLog;
    }
}
