///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2019 All Rights Reserved.
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
//import org.apache.commons.lang3.StringUtils;
//import com.alibaba.fastjson2.JSON;
//import com.alipay.test.acts.model.VirtualArgs;
//import com.alipay.test.acts.model.VirtualObject;
//import com.alipay.test.acts.model.VirtualResult;
//import com.alipay.test.acts.playback.model.PlaybackPrepareData;
//import com.alipay.test.acts.playback.util.SerializeUtil;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 用例数据转换
// *
// * @author qingqin
// * @version $Id: CaseDataConver.java, v 0.1 2019年07月23日 下午5:38 qingqin Exp $
// */
//public class CaseDataConver {
//
//    /**
//     *
//     * @param caseData 用例数据
//     * @param ppd 待构造的对象
//     * @return 如果不符合构造要求，则返回null，如果满足构造要求，把传入的待构造对象进行填充
//     *
//     * @throws IOException 序列化失败
//     * @throws ClassNotFoundException 类加载异常
//     */
//    public static PlaybackPrepareData conver(CaseData caseData, PlaybackPrepareData ppd) throws IOException, ClassNotFoundException {
//
//        if (caseData == null || ppd == null) {
//            return null;
//        }
//
//        //入参数据
//        String[] inputs = new String[] {caseData.getInputParamsByte()};
//        if (inputs == null || inputs.length == 0) {
//            return null;
//        }
//
//        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//
//        //入参类型的数据
//        String[] inputTypes = caseData.getInputParamsType();
//
//        //数据序列化方式
//        SerializedType serializedType = caseData.getSerializedType();
//
//        //转化为真实入参 obj
//        List<Object> arguments = new ArrayList<Object>(inputs.length);
//
//
//        //出参数据
//        String rtnStr = caseData.getReturnParamsByte();
//
//        //出参类型的数据
//        String rtnType = caseData.getReturnParamsType();
//
//        //转化为真实返参 obj
//        Object rtnObjc = null;
//
//        if (serializedType == SerializedType.JSON) {
//            for (int i=0; i<inputs.length; i++) {
//                Class type = Class.forName(inputTypes[i], true, loader);
//                arguments.add(JSON.parseArray(inputs[i], type));
//            }
//
//            // msg无返回值除外
//            if (StringUtils.isNotEmpty(rtnStr)) {
//                Class type = Class.forName(rtnType, true, loader);
//                rtnObjc = JSON.parseArray(rtnStr, type);
//            }
//        } else {
//            //hessian data
//            Object obj = SerializeUtil.deserializeGenericFromString(inputs[0]);
//            obj = SerializeUtil.deGeneralized(obj);
//            Object[] args = (Object[]) obj;
//
//            arguments.addAll(Arrays.asList(args));
//
//            // msg无返回值除外
//            if (StringUtils.isNotEmpty(rtnStr)) {
//                rtnObjc = SerializeUtil.deserializeGenericFromString(rtnStr);
//                rtnObjc = SerializeUtil.deGeneralized(rtnObjc);
//            }
//        }
//
//        //组装 prepare data
//        construct(ppd, arguments, rtnObjc);
//        return ppd;
//
//    }
//
//    private static void construct(PlaybackPrepareData ppd, List<Object> inputs, Object rtn) {
//
//        VirtualArgs virtualArgs = ppd.getArgs();
//
//        for (Object arg:inputs) {
//            virtualArgs.addArg(arg);
//        }
//
//        // msg无返回值除外
//        if (null != rtn){
//            VirtualResult virtualResult = ppd.getExpectResult();
//            virtualResult.setResult(new VirtualObject(rtn));
//        }
//    }
//}
