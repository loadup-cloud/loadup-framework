/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils.config;

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
import com.github.loadup.components.testify.utils.config.impl.TestXMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取系统配置参数缓存
 *
 *
 *
 * Exp $
 */
public class PropertyConfig {

    public static Configration testConfigs = null;
    public static String DB_MODE = "devdb";
    protected static Logger logger = LoggerFactory.getLogger(PropertyConfig.class);
    private static String dbconfFile;

    /**
     * 加载配置文件参数,根据配置项加载不同检验数据连接
     */
    public static synchronized void initConfigs() {

        // 将配置项入缓存，只加载一次
        if (null == testConfigs) {
            testConfigs = ConfigrationFactory.getConfigration();
        }

        if (null != testConfigs) {
            // 可配置数据源文件参数(自由度很高)
            dbconfFile = testConfigs.getPropertyValue(TestifyConstants.DB_CONF_KEY);

            if (StringUtils.isNotBlank(dbconfFile)) {
                // 获取数据库环境模式标记（DB_MODE）
                DB_MODE = getDbMode(dbconfFile);
                // 为了防止自动切换环境出错,添加开关控制
                boolean switchEnv = openSwitchEnv();
                if (switchEnv) {
                    dbconfFile = TestifyConstants.DB_CONF_DIR + dbconfFile.trim();
                    ConfigrationFactory.loadFromConfig(dbconfFile);
                } else {
                    // 完全采用version<=1.73之前的模式
                    dbconfFile = TestifyConstants.TESTIFY_CONFIG_BASE_DIR + dbconfFile.trim();
                    ConfigrationFactory.loadFromConfig(dbconfFile);
                }
            } else {
                dbconfFile = TestifyConstants.TESTIFY_CONFIG_BASE_DIR + TestifyConstants.TESTIFY_CONFIG_FILE_NAME;
                logger.info("ACTS is not contain [dbconf_file]!");
            }
            // 默认按照用例进行控制
            testConfigs.setProperty("filePath", "");
        }
    }

    /**
     * 根据数据库环境模式标记（DB_MODE），定位sofa-test-config.properties文件匹配规则
     *
     * @return
     */
    public static String[] getTestConfigPattern() {
        if (null == testConfigs) {
            initConfigs();
        }
        String configPattern = DB_MODE + "-sofa-test-config.properties";
        String sofaTestMode = System.getProperty("sofatest.ats.db.mode", "");
        if (!StringUtils.isEmpty(sofaTestMode)) {
            configPattern = sofaTestMode + "-sofa-test-config.properties";
        }
        logger.info("ACTS config init :sofaTestConfigFile = " + TestifyConstants.SOFA_TEST_CONFIG_DIR + configPattern);
        return new String[] {TestifyConstants.SOFA_TEST_CONFIG_DIR, configPattern};
    }

    /**
     * 根据数据源文件name取得数据库环境模式标记
     *
     * @param dbconfFile
     * @return
     *
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
        logger.info("ATS配置初始化： DB_MODE = " + dbMode);
        return dbMode;
    }

    /**
     * 判断核心服务测试模式
     *
     * @return true:外部WS模式 false:内部JVM模式
     */
    public static boolean isExteralModel() {
        if (null == testConfigs) {
            initConfigs();
        }
        String testXmode = ConfigrationFactory.getSofaConfig(TestifyConstants.TEST_XMODE);
        TestXMode xmode = TestXMode.byName(testXmode);
        if (TestXMode.RPC.equals(xmode)) {
            return true;
        } else {
            return false;
        }
    }

    /*** 判断是否是tr测试模式* @return true:tr模式 false:ws模式 */
    public static boolean isTRModel() {
        if (null == testConfigs) {
            initConfigs();
        }
        if (null != testConfigs
                && testConfigs.getPropertyValue(TestifyConstants.TR_MODE) != null
                && "true"
                        .equals(testConfigs
                                .getPropertyValue(TestifyConstants.TR_MODE)
                                .trim())) {
            return true;
        } else return false;
    }

    /**
     * 判断是否支持自动切换环境
     *
     * @return true, 默认, 支持切换
     */
    public static boolean openSwitchEnv() {
        if (null == testConfigs) {
            initConfigs();
        }
        if (null != testConfigs
                && testConfigs.getPropertyValue(TestifyConstants.SWITCH_ENV) != null
                && "false"
                        .equals(testConfigs
                                .getPropertyValue(TestifyConstants.SWITCH_ENV)
                                .trim())) {
            return false;
        }
        return true;
    }

    /**
     * 获取数据配置文件
     */
    public static String getDBConfigFile() {
        if (null == testConfigs) {
            initConfigs();
        }
        String dbConfFile = testConfigs.getPropertyValue(TestifyConstants.DB_CONF_KEY);
        if (StringUtils.isBlank(dbConfFile)) {
            return null;
        }
        return dbConfFile;
    }

    /**
     * 读取Config
     *
     * @param key 需要读取的Config
     * @return
     */
    public static String getConfig(String key) {
        if (null == testConfigs) {
            initConfigs();
        }
        String value = testConfigs.getPropertyValue(key);
        return value;
    }

    /**
     * 设置或修改一组值
     *
     * @param key
     * @param value
     */
    public static void setConfig(String key, String value) {
        if (null == testConfigs) {
            initConfigs();
        }
        testConfigs.setProperty(key, value);
    }

    /**
     * 获取配置数据库信息的文件
     *
     * @return
     */
    public static String getDbconfFile() {
        if (null == testConfigs) {
            initConfigs();
        }
        return dbconfFile;
    }
}
