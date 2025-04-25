package com.github.loadup.modules.upms.test.userservice.getuserbyusermobile;

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
public class GetUserByUserMobileTest extends LoadupUpmsTestBase {

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
	 * {@link com.github.loadup.modules.upms.client.api.UserService#getUserByUserMobile(com.github.loadup.modules.upms.client.query.UserMobileQuery)}
	 **/
	@Test(dataProvider = "TestifyDataProvider")
	@AutoFill(overwrite = false, sqlList = {})
    @RunOnly(caseList = {".*"})  // 支持正则匹配，且仅在本地调试时生效
	public void getUserByUserMobile(String caseId, String desc, PrepareData prepareData) {
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
 