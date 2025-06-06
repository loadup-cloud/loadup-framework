/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.util;

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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 *
 *
 */
public class HttpTest {

    public static void main(String[] args) {
        //
        //
        // Map<String, Object> body = new HashMap<String, Object>();
        // List<String> list = new ArrayList<String>();
        //
        //// app
        // body.put("condition", Condition.APP);
        // body.put("conditionValue", "cloancore");
        //
        //// api
        // body.put("condition", Condition.API);
        // body.put("conditionValue", JSON.toJSONString(list));
        //
        ////// case
        //// list.add("74e5cf1a3b107cd0b5350a3d4298229f");
        //// body.put("condition", Condition.SCENE);
        //// body.put("conditionValue", JSON.toJSONString(list));
        //
        //
        // String res = HttpUtil.doPost(host+"/playback/queryPlaybackCaseRefs.json", body);
        //
        // System.out.println(res);
        // System.out.println("");
        //
        // JSONArray rtnObj = extractRtnObj(res);
        //
        // List caseRefs = new ArrayList<CaseRef>();
        //
        // for(Object caseRef : rtnObj) {
        //    caseRefs.add(JsonUtil.stringToObject(((JSONObject)caseRef).toJSONString(),
        // CaseRef.class));
        // }
        //
        // body.clear();
        // body.put("caseRefs", caseRefs);
        // res = HttpUtil.doPost(host+"/playback/queryPlaybackCaseDataByCaseRef.json", body);
        // System.out.println(res);
        // System.out.println("");
        //
        // rtnObj = extractRtnObj(res);
        // List caseDatas = new ArrayList<CaseData>();
        // for (Object caseData : rtnObj) {
        //    caseDatas.add(JsonUtil.stringToObject(((JSONObject)caseData).toJSONString(),
        // CaseData.class));
        // }
        //
        //
        // body.clear();
        // Map md5s = new HashMap<String, Map>();
        // Map vals = new HashMap<String, String>();
        // vals.put("runStatus", "F");
        // vals.put("failCnt", "2");
        // md5s.put("21a20c0b2697113e26cd4cd2001ad8c0", vals);
        // body.put("md5s", md5s);
        // res = HttpUtil.doPost(host+"/playback/updatePlaybackCaseData.json", body);
        // System.out.println(res);

        // body
        JSONObject request = new JSONObject();
        request.put("app", "aeicore");
        String res = HttpUtil.doPost("http://etexxxx.net:80/sceneanalysis/scene/rule.json", request);
        res = res.replaceAll("&quot;", "\\\\\"");
        System.out.println(res);
    }

    public static JSONArray extractRtnObj(String res) {
        JSONObject response = (JSONObject) JsonUtil.toMap(res).get("generalResponse");

        if ((Boolean) response.get("success")) {
            return (JSONArray) response.get("result");
        }
        System.out.println("!!!!!!!!!Error Message: " + response.get("errorMessage"));
        return null;
    }
}
