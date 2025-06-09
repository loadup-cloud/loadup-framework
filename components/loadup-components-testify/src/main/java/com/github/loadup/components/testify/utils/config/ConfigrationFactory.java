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
import com.github.loadup.components.testify.utils.config.impl.ConfigrationImpl;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class ConfigrationFactory {

    public static final String ATS_CONFIG_SYSENV_PREFIX = "ats.config.";
    public static final String SOFA_CONFIG_SYSENV_PREFIX = "sofatest.";
    public static final String ATS_EXT_CONFIG_KEY = "ext_config_file";
    protected static Logger logger = LoggerFactory.getLogger(ConfigrationFactory.class);
    private static Configration configImpl;

    /**
     * 取得Configration对象
     * <p>
     * 不需要加同步处理，Configration一般都是在系统启动时进行加载，启动的时候为单线程加载
     *
     * @return
     */
    public static Configration getConfigration() {
        if (configImpl == null) {
            configImpl = new ConfigrationImpl();
            // 从配置文件或环境变量中加载配置
            // 环境变量优先级高于配置文件
            // 环境变量是以 ats.config 为前缀的变量
            loadFromConfig(TestifyConstants.TESTIFY_CONFIG_BASE_DIR + TestifyConstants.TESTIFY_CONFIG_FILE_NAME);
        }
        // 允许加载额外配置文件
        String extConfs = configImpl.getPropertyValue(ATS_EXT_CONFIG_KEY);
        if (StringUtils.isNotBlank(extConfs)) {
            String[] files = extConfs.split(",");
            for (String confName : files) {
                loadFromConfig(TestifyConstants.TESTIFY_CONFIG_BASE_DIR + confName);
            }
        }
        return configImpl;
    }

    /**
     * 从配置文件中加载配置
     * <p>
     * 一般就是ats-config.properties文件
     */
    public static void loadFromConfig(String confName) {
        logger.info("loading configuration [" + confName + "]");

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        URL atsConfigUrl = currentClassLoader.getResource(confName);

        //        if (atsConfigUrl == null) {
        //            logger.error("can not find ats config [" + confName + "]!");
        //            return;
        //        }

        Properties atsProperties = new Properties();
        //        try {
        //            atsProperties.load(atsConfigUrl.openStream());
        //        } catch (Exception e) {
        //            logger.error("can not find ats config [" + confName + "] details [" + e.getMessage()
        //                         + "]");
        //            return;
        //        }

        Set<Map.Entry<Object, Object>> entrySet = atsProperties.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            Object keyObject = entry.getKey();
            Object valueObject = entry.getValue();
            String envConfigValue = System.getProperty(ATS_CONFIG_SYSENV_PREFIX + keyObject.toString());
            if (StringUtils.isNotBlank(envConfigValue)) {
                configImpl.setProperty(keyObject.toString(), envConfigValue);
            } else {
                configImpl.setProperty(keyObject.toString(), valueObject.toString());
            }
        }
    }

    /**
     * 获取Sofa配置项
     */
    public static String getSofaConfig(String keyName) {
        String sofaConfig = ""; // TestUtils.getSofaConfig(keyName);
        String envSofaValue = System.getProperty(SOFA_CONFIG_SYSENV_PREFIX + keyName.toString());
        if (StringUtils.isNotBlank(envSofaValue)) {
            return envSofaValue;
        } else {
            return sofaConfig;
        }
    }
}
