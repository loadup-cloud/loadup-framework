/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.event.ssh;

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

import com.github.loadup.components.testify.utils.config.ConfigrationFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jie.peng
 *
 */
public class AppServerLogFetcher extends RemoteLogFecther {

    /**
     * 从服务器日志提取目标信息
     *
     * @param commands 查找命令，</br>
     *                 如grep 201109070028494 /home/admin/logs/opencoreHost/opencore-core-service.log</br>
     *                 <Strong style="color:red">注意，命令中的服务器host统一使用opencoreHost，本方法实现内部会进行实际的opencoreHost替换</Strong>
     * @param targets  待获取字段名称列表
     * @return 返回key-value对
     */
    public Map<String, String> getInfoFromLog(String commands, String... targets) {

        Map<String, String> result = new HashMap<String, String>();
        String log = getLog(commands);
        if (StringUtils.isBlank(log)) {
            return result;
        }
        for (String target : targets) {
            // 若没有过滤出目标内容，则不放进结果map
            String targetValue = ""; // pickup(log, target);
            if (StringUtils.isBlank(targetValue)) {
                continue;
            }
            result.put(target, targetValue);
        }
        return result;
    }

    /**
     * 根据命令获取服务器上信息,注意调用时命令内容不需要指定明确的opencore服务器域名，而是以opencoreHost占位符表示</br>
     * 这里会自动进行替换
     *
     * @param commands 查找命令，</br>
     *                 如grep 201109070028494 /home/admin/logs/opencoreHost/opencore-core-service.log
     * @return
     */
    public String getLog(String commands) {

        String hostName = getOpencoreServer();

        // commands = StringUtils.replace(commands, "opencoreHost", "opencore");

        return getServerLog(hostName, commands);
    }

    /**
     * 获取应用服务器地址，这里根据配置文件配置的xxx_service_url解析出被测远程服务器的域名
     * xxx为应用名
     *
     * @return
     */
    public String getOpencoreServer() {
        String appName = ConfigrationFactory.getSofaConfig("app_name");
        String server = ConfigrationFactory.getSofaConfig(appName + "_service_url");
        if (StringUtils.isBlank(server)) {
            logger.warn("APP service Url configuration not found, check up the config file");
            return null;
        }
        Pattern p = Pattern.compile("://(.*):8080");
        Matcher m = p.matcher(server);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
