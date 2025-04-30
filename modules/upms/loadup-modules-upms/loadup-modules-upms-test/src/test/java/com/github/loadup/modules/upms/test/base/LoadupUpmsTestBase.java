
package com.github.loadup.modules.upms.test.base;

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

import com.github.loadup.components.testify.template.TestifyTestBase;
import com.github.loadup.modules.upms.test.TestApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class LoadupUpmsTestBase extends TestifyTestBase {

    //启动时需要加载的测试依赖
    static {
        System.setProperty("test_artifacts",
                "commons-pool-,commons-beanutils-,commons-dbcp-,guava-,ats-common-,cmmons-io-,atd-app-server-,cloning,objenesis,"
                        + "acts-common-util-,acts-common-dto,acts-core-,itest-common-lang-");
    }


    /**
     * DB预跑反填：覆写该方法，通过设置指定表的select语句条件来捞取该表对应数据，框架会自动将数据返填至用例的CheckDBData目录下作为后续DB比对的依据
     * 此处配置的DB捞取逻辑会在全局生效，若需要针对指定测试用例配置可在对应的用例测试类的runTest方法通过@AutoFill注解的sqlList属性配置
     *
     * @param
     * @return
     */
    @Override
    public List<String> backFillSqlList() {
        List<String> sqlList = new ArrayList<String>();

        /*
        这里把需要查询的sql添加至List，select条件可以通过变量替换方式任意从入参和结果中获取
        - 入参的获取方式为：$args.get(0).getOutOrderNo()
        - 结果的获取方式为：$result.getMasterOrderNo()
        */

        //sqlList.add("select * from fas_order where order_no = '$result.getOrderNo()';");
        //sqlList.add("select * from fas_note where order_no = '$result.getOrderNo()';");

        return sqlList;
    }

    /**
     * 对预期校验项（结果、异常、消息）的某些字段设置不校验，格式:类(子类对象)#属性#校验flag
     * 校验flag：N=不校验；R=正则匹配校验；更多flag请参阅文档
     * 此处配置会在全局生效（所有测试用例）
     *
     * @return
     */
    @Override
    public List<String> setIgnoreCheckFileds() {
        List<String> ignoreCheckFields = new ArrayList<String>();

        // DemoResponse这个类的errMsg字段跳过校验
        //ignoreCheckFields.add("com.xxx.DemoResponse#errMsg#N");
        // DemoResponse这个类的errMsg字段进行正则匹配校验，正则表达式填写至yaml该字段区域
        //ignoreCheckFields.add("com.xxx.DemoResponse#errMsg#R");

        return ignoreCheckFields;
    }


}

 
