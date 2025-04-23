package com.alipay.test.acts.annotation;

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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alipay.test.acts.annotation.acts.AfterCheck;
import com.alipay.test.acts.annotation.acts.AfterClean;
import com.alipay.test.acts.annotation.acts.AfterPrepare;
import com.alipay.test.acts.annotation.acts.BeforeCheck;
import com.alipay.test.acts.annotation.acts.BeforeClean;
import com.alipay.test.acts.annotation.acts.BeforePrepare;
import com.alipay.test.acts.annotation.acts.Executor;
import com.alipay.test.acts.component.handler.TestUnitHandler;
import com.alipay.test.acts.runtime.ActsRuntimeContext;
import com.alipay.test.acts.template.ActsTestBase;

/**
 * executor method, do execute with prepare、execute、check and clean
 * if you has any problem, please contact with xiuzhu.hp in ali-wang-wang 
 * @author hupeng
 *
 */
public class ActsExecutorMethod extends ActsMethodImpl {
	
	protected final Log               logger = LogFactory.getLog(this.getClass());
	
	/**
	 * constructor
	 * @param method
	 * @param instance
	 * @param exeAnnotation
	 */
	public ActsExecutorMethod(Method method, Object instance, Executor executor) {
		super(method, instance);
		this.group = executor.group();
		this.setOrder(executor.Order());
	}

	@Override
    public void invoke(ActsRuntimeContext actsRuntimeContext){
		logger.info("=============================["+this.group+" begin]=============================\r\n");

		/* conversion */
		ActsTestBase testbase = (ActsTestBase)this.instance;
		
		TestUnitHandler testUnitHandler = testbase.getTestUnitHandler();
		
		try {
			
			logger.info("=============================["+this.group+" prepare begin]=============================\r\n");

			/* do prepare */
			try{
				
				testbase.invokeIActsMethods(BeforePrepare.class, actsRuntimeContext);
				testUnitHandler.clearDepData(null, this.group);
				testUnitHandler.prepareDepData(null, this.group);
				
			} finally{
				testbase.invokeIActsMethods(AfterPrepare.class, actsRuntimeContext);
			}
			
			logger.info("=============================["+this.group+" prepare end]=============================\r\n");

			logger.info("=============================["+this.group+" invoke begin]=============================\r\n");

			/* do the method with @Executor on it */
			super.invoke(actsRuntimeContext);
			
			logger.info("=============================["+this.group+" invoke end]=============================\r\n");

			
			logger.info("=============================["+this.group+" check begin]=============================\r\n");

			/* do check */
			testbase.invokeIActsMethods(BeforeCheck.class, actsRuntimeContext);
			
			try{
				testUnitHandler.checkExpectDbData(null, this.group);
			} finally{
				testbase.invokeIActsMethods(AfterCheck.class, actsRuntimeContext);
			}
			
			logger.info("=============================["+this.group+" check end]=============================\r\n");

			
		} finally{
			logger.info("=============================["+this.group+" clean begin]=============================\r\n");

			/* do clean */
			try{
				testbase.invokeIActsMethods(BeforeClean.class, actsRuntimeContext);
				testUnitHandler.clearDepData(null, this.group);
				testUnitHandler.clearExpectDBData(null, this.group);
			}finally{
				
				try {
					testbase.invokeIActsMethods(AfterClean.class, actsRuntimeContext);
				} finally {
					logger.info("=====================["+this.group+" clean end]======================\r\n");
				}
			}
			
		}
		logger.info("=====================["+this.group+" end]======================\r\n");

	}
}
