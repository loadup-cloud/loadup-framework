/**
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.annotation;

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

import com.github.loadup.components.testify.model.PrepareData;
import com.github.loadup.components.testify.utils.CaseResultCollectUtil;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;


public class TestSqlparse {


    @Test
    // 测试解析并保存成yaml
    public void testcollectSqlAndSaveLog1() {
        System.out.println("===========开始测试===========");
        Map<String, PrepareData> caseDatas = new HashMap<String, PrepareData>();
        String caseId = "SfatransManageFacade_openAccount_caseID_001";
        PrepareData prepareData = new PrepareData();
        CaseResultCollectUtil.collectSqlLog(caseId, prepareData);
        // 保存到yaml中
        System.out.println(prepareData);

        System.out.println("===========测试结束===========");
    }

    @Test
    // 测试解析并保存成yaml
    public void testcollectSqlAndSaveLog() {

        Map<String, PrepareData> caseDatas = new HashMap<String, PrepareData>();
        String caseId = "acts_caseId=SfatransTransFacade_deposit_caseID_001";
        PrepareData prepareData = new PrepareData();
        String aa = prepareData.getDescription();
        aa = "测试";
        //prepareData.setDescription(aa);
        System.out.println(prepareData);

        CaseResultCollectUtil.collectSqlLog(caseId, prepareData);
        caseDatas.put("case001", prepareData);
        System.out.println("===========开始执行第2次");
        CaseResultCollectUtil.collectSqlLog(caseId, prepareData);
        caseDatas.put("case002", prepareData);
        System.out.println("===========开始执行第3次");
        CaseResultCollectUtil.collectSqlLog(caseId, prepareData);
        caseDatas.put("case003", prepareData);
        // 保存到yaml中
        String relativePath = "logs/test.yaml";
        //CaseResultCollector.saveCaseResult(relativePath, caseDatas);
        System.out.println(caseDatas);
    }

    @Test
    public void testcollectSqlLog() {

        String caseId = "PreparePayActsTest_preparePay_caseID_001";
        PrepareData prepareData = new PrepareData();
        CaseResultCollectUtil.collectSqlLog(caseId, prepareData);
        System.out.println(prepareData);
    }


}
