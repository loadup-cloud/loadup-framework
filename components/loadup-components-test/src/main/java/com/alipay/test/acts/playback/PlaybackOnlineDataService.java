///**
// * Alipay.com Inc. Copyright (c) 2004-2020 All Rights Reserved.
// */
//package com.alipay.test.acts.playback;
//
///*-
// * #%L
// * loadup-components-test
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;
//import com.alipay.miu.facade.enums.QueryType;
//import com.alipay.miu.facade.model.CaseData;
//import com.alipay.miu.facade.model.CaseIndex;
//import com.alipay.miu.facade.request.QueryCaseData;
//import com.alipay.miu.facade.request.QueryCaseIndex;
//import com.alipay.miu.facade.request.QueryCaseIndexByPage;
//import com.alipay.miu.facade.request.UpdateCasePlayBackResult;
//import com.alipay.test.acts.util.HttpUtil;
//import com.alipay.test.acts.util.JsonUtil;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 线上回放数据获取
// *
// * @author pumo
// * @version $Id: PlaybackOnlineDataService.java, v 0.1 2020年01月15日 11:38 AM pumo Exp $
// */
//public class PlaybackOnlineDataService {
//
//    private static final String PROD_HOST = "https://miuprod.alipay.com/api/open/miu";
//
//    private static final String DEV_HOST = "http://puyu.inc.alipay.net/playback";
//
//    // 未指定case环境默认走线上用例地址
//    private static String HOST = PROD_HOST;
//
//    private static final String QUERY_CASE_REFS = "/case/queryCaseIndex";
//
//    private static final String QUERY_CASE_REFS_GROUP = "/case/queryCaseIndexByPage";
//
//    private static final String QUERY_CASE_DATA = "/case/queryCaseData";
//
//    private static final String UPDATE_CASE_DATA_STATUS = "/case/updateCasePlayBackResult";
//
//    private static final String TRIGGER_PLAYBACK_ANALYSIS = "/triggerPlaybackAnalysis.json";
//
//    public static List<CaseIndex> queryOnlineCaseRefs(QueryType condition, String query) throws Exception {
//
//        List<CaseIndex> caseDataIndexList = new ArrayList<CaseIndex>();
//
//        QueryCaseIndex queryCaseIndexRequest = new QueryCaseIndex();
//        queryCaseIndexRequest.setQueryType(condition);
//        queryCaseIndexRequest.setQueryValue(query);
//
//        String res = HttpUtil.doPostObject(HOST + QUERY_CASE_REFS, queryCaseIndexRequest);
//        JSONArray rtnObj = parseQueryCaseDataIndexResult(res);
//
//        for (Object caseDataIndex : rtnObj) {
//            caseDataIndexList.add(JsonUtil.stringToObject(((JSONObject) caseDataIndex).toJSONString(), CaseIndex.class));
//        }
//
//        return caseDataIndexList;
//
//    }
//
//    public static List<CaseIndex> queryOnlineCaseRefsByGroup(QueryType condition, String query,
//                                                                 int totalPage, int currentPage) throws Exception {
//
//        List<CaseIndex> caseDataIndexList = new ArrayList<CaseIndex>();
//
//        QueryCaseIndexByPage queryCaseDataIndexPageRequest = new QueryCaseIndexByPage();
//        queryCaseDataIndexPageRequest.setQueryType(condition);
//        queryCaseDataIndexPageRequest.setQueryValue(query);
//        queryCaseDataIndexPageRequest.setTotalPage(totalPage);
//        queryCaseDataIndexPageRequest.setCurrentPage(currentPage);
//
//        String res = HttpUtil.doPostObject(HOST + QUERY_CASE_REFS_GROUP, queryCaseDataIndexPageRequest);
//        JSONArray rtnObj = parseQueryCaseDataIndexResult(res);
//
//        for (Object caseDataIndex : rtnObj) {
//            caseDataIndexList.add(JsonUtil.stringToObject(((JSONObject) caseDataIndex).toJSONString(), CaseIndex.class));
//        }
//
//        return caseDataIndexList;
//
//    }
//
//    public static CaseData queryOnlineCaseData(CaseIndex caseDataIndex) throws Exception {
//
//        QueryCaseData queryCaseDataRequest = new QueryCaseData();
//
//        queryCaseDataRequest.setCaseId(caseDataIndex.getCaseId());
//
//        String res = HttpUtil.doPostObject(HOST + QUERY_CASE_DATA, queryCaseDataRequest);
//        return JSONObject.parseObject(parseQueryCaseDataResult(res), CaseData.class);
//
//    }
//
//    public static boolean updateCaseDataStatus(String caseId, String traceId, boolean isSuccess, String errMsg) throws Exception {
//
//        String status = "F";
//        if (isSuccess) {
//            status = "S";
//        }
//
//
//        UpdateCasePlayBackResult updateCaseDataPlayBackDataRequest = new UpdateCasePlayBackResult();
//        updateCaseDataPlayBackDataRequest.setCaseId(caseId);
//        updateCaseDataPlayBackDataRequest.setRunStatus(status);
//        updateCaseDataPlayBackDataRequest.setPlaybackTraceId(traceId);
//        updateCaseDataPlayBackDataRequest.setRunDetail(errMsg);
//
//        int i;
//        for (i = 0; i < 3; i++) {
//            String res = HttpUtil.doPostObject(HOST + UPDATE_CASE_DATA_STATUS, updateCaseDataPlayBackDataRequest);
//            if (parseUpdateCaseDataResult(res)) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }
//
//    public static boolean triggerPlaybackAnalysis(String appName) throws Exception {
//        Map<String, Object> body = new HashMap<String, Object>();
//        body.put("projectName", appName);
//
//        String res = HttpUtil.doPost(HOST + TRIGGER_PLAYBACK_ANALYSIS, body);
//
//        JSONObject response = (JSONObject) JsonUtil.toMap(res).get("generalResponse");
//
//        if ((Boolean)response.get("success")) {
//            return true;
//        }
//
//        Exception e = new Exception(res);
//        throw e;
//    }
//
//    /**
//     * 设置用例获取链路，默认从线上地址读取用例（返回线上录制的用例）
//     * @param isDevCase
//     */
//    public static void setCaseSrc(boolean isDevCase) {
//        HOST = isDevCase ? DEV_HOST : PROD_HOST;
//    }
//
//    /**
//     * 解析用力索引查询结果
//     * @param res
//     * @return
//     * @throws Exception
//     */
//    private static JSONArray parseQueryCaseDataIndexResult(String res) throws Exception {
//        JSONObject response = JSONObject.parseObject(res);
//
//        if (response.getBoolean("success")) {
//            return (JSONArray) response.get("data");
//        }
//
//        return null;
//
//    }
//
//    /**
//     * 解析用例数据查询结果
//     * @param res
//     * @return
//     * @throws Exception
//     */
//    private static Boolean parseUpdateCaseDataResult(String res) throws Exception {
//        JSONObject response = JSONObject.parseObject(res);
//
//        if (response.getBoolean("success")) {
//            return response.getBoolean("data");
//        }
//
//        return false;
//
//    }
//
//    /**
//     * 解析用例回放结果更新回写结果
//     * @param res
//     * @return
//     * @throws Exception
//     */
//    private static String parseQueryCaseDataResult(String res) throws Exception {
//        JSONObject response = JSONObject.parseObject(res);
//
//        if (response.getBoolean("success")) {
//            return response.get("data").toString();
//        }
//
//        return "";
//
//    }
//
//}
