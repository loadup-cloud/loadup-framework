/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.api;

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

import com.alibaba.fastjson2.JSON;
import com.github.loadup.components.testify.data.MetaItemAutoBuilder;
import com.github.loadup.components.testify.data.RuleDataFactory;
import java.util.List;
import java.util.Map;

/**
 * 数据规则操作的api
 *
 *
 *
 */
public class DataRuleApis {

    /****
     * 根据系统名和工程路径初始化原子库
     * 1.找到路径下的config-dal文件,解析出来数据库信息.然后根据查询出来的元数据解析
     * 2.找到工程的common/facade路径,解析里面的类,入参,结果.等
     * @param system
     * @param projectPath
     */
    public static void generateAtomRule(String system, String projectPath) {

        MetaItemAutoBuilder builder = new MetaItemAutoBuilder();
        builder.generateDataRule(system, projectPath);
    }

    /*****
     * 根据系统名和工程路径找到objModel 和 dbModel文件夹,
     * 然后更新里面的source_rule到数据库
     *
     * @param system
     * @param projectPath
     * @return
     */
    public static boolean updateAtomRule(String system, String projectPath) {

        MetaItemAutoBuilder builder = new MetaItemAutoBuilder();
        builder.updateDbDataRule(system, projectPath);
        builder.updateObjDataRule(system, projectPath);
        return true;
    }

    /*****
     * 规则试算接口.用于试算写的规则能够生成的值
     *
     * @param system
     * @param caseRule
     * @return
     */
    public static List<Map<String, String>> tryCalculate(String system, String caseRule) {
        String rowData = new RuleDataFactory().getDataByOriginRule(system, null, caseRule);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> fieldValues = (List<Map<String, String>>) JSON.parse(rowData);
        return fieldValues;
    }
}
