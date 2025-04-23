package com.alipay.test.acts.playback.util;

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


/**
 *
 * @author qingqin
 * @version $Id: PlayBackActsTestBase.java, v 0.1 2019年08月02日 下午21:16 qingqin Exp $
 */
public class ActsDummyContextUtil {

    /**
     *
     * @throws Exception 发生异常
     */
    public static void createDummyLogContext() throws Exception {

        ActsDummyLogContext actsDummyLogContext = new ActsDummyLogContext();
//        actsDummyLogContext.setTraceId(TraceIdGenerator.generate());
//        actsDummyLogContext.setRpcId(Tracer.ROOT_RPC_ID);
//        AbstractLogContext.set(actsDummyLogContext);
    }

    /**
     * 
     * @throws Exception DummyLogContext
     */
    public static void clearDummyLogContext() throws Exception {
//        AbstractLogContext abstractLogContext = AbstractLogContext.get();
//
//        if (abstractLogContext == null) {
//            return;
//        }
//
//        if (abstractLogContext instanceof ActsDummyLogContext) {
//            AbstractLogContext.set(null);
//            return;
//        }
    }

    /**
     * 
     * @param penAttrKey  Key
     * @param value Value
     * @throws Exception DummyLogContext
     */
    public static void addPenetrateAttribute(String penAttrKey, String value)
                                                                                         throws Exception {
//        AbstractLogContext abstractLogContext = AbstractLogContext.get();
//
//        if (abstractLogContext == null) {
//            throw new Exception("Tracer");
//        }
//
//        if (abstractLogContext instanceof ActsDummyLogContext) {
//            abstractLogContext.addPenetrateAttribute(penAttrKey, value);
//            return;
//        }
//
//        throw new Exception("Tracer  DummyLogContext:"
//                            + abstractLogContext.getClass());
    }

    private static class ActsDummyLogContext {
//            extends AbstractLogContext<ActsDummyLogContext> {
//
//        @Override
//        protected ActsDummyLogContext getDefaultLogContext() {
//            return null;
//        }
//
//        @Override
//        public StatKey getStatKey() {
//            return null;
//        }
//
//        @Override
//        public boolean isSuccess() {
//            return false;
//        }
//        //mock
//        public AbstractLogContext cloneInstance() {
//            return this;
//        }
    }

}
