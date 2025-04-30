
package com.github.loadup.modules.upms.test;

/*-
 * #%L
 * loadup-modules-upms-test
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

import com.github.loadup.components.testify.util.TestifyCommonUtil;
import com.github.loadup.modules.upms.client.api.UserService;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单类
 */
public class TestifyMenu {
    /*** 此处替换成自己的应用名 ***/
    public static String appName = "loadup";

    /**
     * 初始化应用的环境配置文件
     *
     * @throws Exception
     */
    @Test
    public void initTestify() throws Exception {
        // 生成接口下所有方法的测试脚本
        TestifyCommonUtil.initTestify(appName, "upms", true);
    }

    /**
     * 根据接口和方法生成测试用例
     *
     * @throws Exception
     */
    @Test
    public void genScript() throws Exception {
        // 方式一：生成接口下所有方法的测试脚本
        TestifyCommonUtil.genTestScript(UserService.class, appName, "upms");

        // 方式二：生成接口下某个方法的测试脚本
        // ActsCommonUtil.genTestScript(YourFacade.class, "yourMethodName", appName);

        // 方式三：高阶用法，可自定义生成路径和测试脚本模板
        // 第四个参数为自定义路径，第五个参数为Velocity模板内容，第六个参数为模板替换上下文
        // 具体入参类型详见接口实现，无需自定义的部分可直接填写null
        // ActsCommonUtil.genTestScript(YourFacade.class, "yourMethodName", appName, 
        //        "com.xxx.xx", null, null);
    }

    /**
     * 通过对象自动转换生成yaml文件
     */
    @Test
    public void genObjYaml() {
        // 自行构建对象
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> m = new HashMap<>();
        m.put("eventCode", "EC_ORDER_PAY");
        m.put("topicId", "TP_P_FAS");
        m.put("isExist", "N");
        list.add(m);

        // 转换成yaml字符串，执行后会自动复制内容到剪切板
        TestifyCommonUtil.genObjToYamlStr(list);
    }

    /**
     * 生成可供Yaml内直接粘贴的Flags字符串
     */
    @Test
    public void genFlags() {
        List flag = new ArrayList<String>();
        // 需要配置flag的字段
        //flag.add("errMsg");
        //flag.add("success");
        //
        //// 转换成yaml字符串，执行后会自动复制内容，请手动粘贴至yaml文件内对应位置
        //String flags = ActsCommonUtil.genFlags(YourResponse.class, flag);
        //System.out.println(flags);
    }

    /**
     * 通过guid生成表模板结构
     *
     * @throws Exception
     */
    @Test
    public void genDBModel() throws Exception {
        List<String> gs = new ArrayList<>();
        gs.add("IDB_L_519014.obfascore_lfo.fas_pledge_operation");
        gs.add("IDB_L_519017.obfascore_lfo.business_activity");

        TestifyCommonUtil.genDBTableCols(gs);
    }
}
