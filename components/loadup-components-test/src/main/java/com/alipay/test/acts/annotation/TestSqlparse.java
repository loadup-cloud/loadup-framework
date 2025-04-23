/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.alipay.test.acts.model.PrepareData;
import com.alipay.test.acts.utils.CaseResultCollectUtil;


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
		aa= "测试";
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
	
	
	
//	@Test
//	// 测试解析并保存成yaml
//	public void testLoad() {
//		
//		ActsRuntimeContext actsRuntimeContext = new ActsRuntimeContext();
//		actsRuntimeContext
//				.setCaseId("PreparePayActsTest_preparePay_caseID_001");
//		actsRuntimeContext.setResultObj(null);
//		CaseResultCollectUtil
//				.holdProcessData(actsRuntimeContext.getCaseId(), null,
//						actsRuntimeContext.getResultObj(), "logs/test.yaml",
//						null);
//
//		System.out.println(actsRuntimeContext);
//	}

	// @Test
	// public void testpaycoreSqlParse() {
	// List<String> singleSqlExecLog = new ArrayList<String>();
	// SqlLogParser sqlLogParser = new InsertSqlLogParser();
	// // 结尾有空格的情况
	// singleSqlExecLog
	// .add(0,
	// "23:49:20,196 [main] DEBUG JakartaCommonsLoggingImpl : {pstm-100350} Executing Statement:               insert into pmt_pay_ord(payment_id,pay_request_id,biz_product,biz_no,biz_action_type,biz_action_no,payment_type,partner_biz_no,partner_id,payer_id,payer_id_type,payee_id,payee_id_type,pay_amount,currency,status,trans_status,gmt_payment,pay_terminal,tx_id,refund_amount,refund_inprocess_amount,gmt_last_refund,pay_error_code,inst_error_code,payment_extra,gmt_create,gmt_modified,cmpt_error_code,gmt_bizdate,memo,biz_mode,original_biz_no,biz_process_id,real_amount,principal_id,db_info,ack_tx_id,ack_trans_status,transaction_cache,relate_payment_id,APPLICATION_SCENE,BUSINESS_SOURCE_SYS) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	// singleSqlExecLog
	// .add(1, "");
	// singleSqlExecLog
	// .add(2,
	// "23:49:20,200 [main] DEBUG JakartaCommonsLoggingImpl : {pstm-100350} Types: [java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.String, java.lang.String, java.lang.String, null, java.lang.String, null, java.lang.Long, java.lang.Long, null, null, null, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, null, java.sql.Timestamp, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.String, null, null, java.lang.String, java.lang.String, java.lang.String, null, java.lang.String]");
	// SqlLogCollector.preprocessSqlLog(singleSqlExecLog);
	//
	// List<Map<String, Object>> tableDatas = sqlLogParser.parseGenTableDatas(
	// singleSqlExecLog.get(0), SqlLogParseFactory.parseSqlParamValue(
	// singleSqlExecLog.get(1), singleSqlExecLog.get(2)),
	// Arrays.asList(singleSqlExecLog.get(2).split(", ")));
	//
	// System.out.println(singleSqlExecLog);
	// System.out.println(tableDatas);
	//
	// }
	//
	//
	//
	// @Test
	// public void testSqlParse() {
	// List<String> singleSqlExecLog = new ArrayList<String>();
	// SqlLogParser sqlLogParser = new InsertSqlLogParser();
	// // 结尾有空格的情况
	// singleSqlExecLog
	// .add(0,
	// "14:41:44,546 [main] DEBUG JakartaCommonsLoggingImpl : {pstm-100443} Executing Statement:               insert into deposit_exchange_data(deposit_id,influx_id,alipay_err_code,alipay_err_desc,inst_err_code,inst_err_desc,gmt_create,gmt_modified,bank_extra_context) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
	// singleSqlExecLog
	// .add(1,
	// "14:41:44,548 [main] DEBUG JakartaCommonsLoggingImpl : {pstm-100443} Parameters: [20151214A0067046, 20151214975290283184050330250747, null, null, null, null, 2015-12-14 14:41:44.093, 2015-12-14 14:41:44.093, ]");
	// singleSqlExecLog
	// .add(2,
	// "14:41:44,550 [main] DEBUG JakartaCommonsLoggingImpl : {pstm-100443} Types: [java.lang.String, java.lang.String, null, null, null, null, java.sql.Timestamp, java.sql.Timestamp, java.lang.String]");
	// SqlLogCollector.preprocessSqlLog(singleSqlExecLog);
	//
	// List<Map<String, Object>> tableDatas = sqlLogParser.parseGenTableDatas(
	// singleSqlExecLog.get(0), SqlLogParseFactory.parseSqlParamValue(
	// singleSqlExecLog.get(1), singleSqlExecLog.get(2)),
	// Arrays.asList(singleSqlExecLog.get(2).split(", ")));
	//
	// System.out.println(singleSqlExecLog);
	// System.out.println(tableDatas);
	//
	// }
	//
	// @Test
	// public void testinsertSql() {
	// SqlLogParser sqlLogParser = new InsertSqlLogParser();
	//
	// String sql =
	// "insert into deposit_exchange_data(gmt_create,gmt_modified,bank_extra_context) values (?, ?, ?)";
	// List<String> paramValue = new ArrayList<String>();
	// paramValue.add("2015-12-14 14:36:40.905");
	// paramValue.add("2015-12-14 14:36:40.905");
	// // paramValue.add("");
	// List<String> paramType = new ArrayList<String>();
	// paramType.add("java.sql.Timestamp");
	// paramType.add("java.sql.Timestamp");
	// paramType.add("java.sql.Timestamp");
	// List<Map<String, Object>> result = sqlLogParser.parseGenTableDatas(sql,
	// paramValue, paramType);
	// System.out.println(result);
	// }
	//
	// @Test
	// public void testListChooseRule() {
	// SqlLogParser sqlLogParser = new InsertSqlLogParser();
	// String sql =
	// "insert into deposit_instruction_unique(instruction_id,pay_channel_api,gmt_create,aa) values (?, ?, sysdate,aa)";
	// List<String> paramValue = new ArrayList<String>();
	// paramValue.add("201512110000000000034519034601");
	// paramValue.add("icbc1010");
	// List<String> paramType = new ArrayList<String>();
	// paramType.add("java.lang.String");
	// paramType.add("java.lang.String");
	// List<Map<String, Object>> result = sqlLogParser.parseGenTableDatas(sql,
	// paramValue, paramType);
	// System.out.println(result);
	//
	// }
}
