package com.github.loadup.modules.upms.test.userservice.getuserbydeptids;

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

import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import com.github.loadup.modules.upms.test.base.LoadupUpmsTestBase;
import com.github.loadup.components.testify.annotation.TestBean;
import com.github.loadup.components.testify.annotation.testify.RunOnly;
import com.github.loadup.components.testify.annotation.testify.PrepareCase;
import com.github.loadup.components.testify.model.PrepareData;
import org.testng.annotations.Test;
import com.github.loadup.components.testify.annotation.testify.AutoFill;
import jakarta.annotation.Resource;
import com.github.loadup.modules.upms.client.api.UserService;

/**
 *
 * @author ACTS
 *
 */
public class GetUserByDeptIdsTest extends LoadupUpmsTestBase {

	@TestBean
	@Resource
	protected UserService userService;

	/**
	 * runTest说明：脚本中的process方法中的clear,prepare,execute,check四个方法如果无法满足脚本
	 * 需求可以进行重写。
	 * For example:
	 * public void prepare(ActsRuntimeContext actsRuntimeContext) throws ActsTestException {
	 *      userDefined();//自定义数据准备;
	 *      super.prepare(actsRuntimeContext);//继承父类数据准备方法
	 * }
	 *
	 *
	 * 预跑反填说明：
	 *  @AutoFill(overwrite = false, sqlList = {})
	 *  - 如果需要重写预跑反填数据需要设置: overwrite = true
	 *  - sqlList可填写仅针对本测试方法的校验db数据的捞取逻辑，更多解释可参考基类的backFillSqlList方法注释
	 *
	 * {@link com.github.loadup.modules.upms.client.api.UserService#getUserByDeptIds(com.github.loadup.modules.upms.client.query.UserDeptListQuery)}
	 **/
	@Test(dataProvider = "TestifyDataProvider")
	@AutoFill(overwrite = false, sqlList = {})
    @RunOnly(caseList = {".*"})  // 支持正则匹配，且仅在本地调试时生效
	public void getUserByDeptIds(String caseId, String desc, PrepareData prepareData) {
		runTest(caseId, prepareData);
	}



	/**
	 * 可覆写该方法，添加一些用例执行前的自定义逻辑，如特殊入参设置、服务mock等
	 *
	 * @param testifyRuntimeContext
	 */
	@Override
	public void beforeTestifyTest(TestifyRuntimeContext testifyRuntimeContext) {
        super.beforeTestifyTest(testifyRuntimeContext);
		//String caseId = actsRuntimeContext.getCaseId();
        //
		//if (caseId.equals("case01")) {
		//	// 重新设置用例入参对象
		//	actsRuntimeContext.setArg(0, "set first arg by hand");
        //
		//	// Mock service by Mockito
		//
		//}
	}

    // 在用例执行前，正则匹配到case01时执行以下逻辑做数据准备，效果同beforeActsTest内逻辑
    @PrepareCase(".*case01.*")
    public void p01(TestifyRuntimeContext testifyRuntimeContext) {
	    // actsRuntimeContext.setArg(0, "set first arg by hand");
    }

	/**
	 * 可覆写该方法，添加一些用例执行后的自定义逻辑，如清理Mock等
	 *
	 * @param context
	 */
	@Override
	public void afterTestifyTest(TestifyRuntimeContext context) {
        super.afterTestifyTest(context);
	}

}
 
