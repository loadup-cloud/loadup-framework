package com.github.loadup.components.testify.model;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 虚拟参数对象
 *
 * @author tantian.wc
 *
 */
public class VirtualArgs extends TestUnit {

    /**
     * 按照顺序排列的入参列表
     */
    public List<VirtualObject> inputArgs;

    public VirtualArgs() {
        super();
    }

    /**
     * 构造器直接创建入参列表
     *
     * @param inputArgs
     */
    public VirtualArgs(List<Object> inputArgs) {
        this.setInputArgs(inputArgs);
    }

    public static VirtualArgs getInstance() {
        return new VirtualArgs();
    }

    /**
     * 添加一个参数
     *
     * @param obj
     * @return
     */
    public VirtualArgs addArg(Object obj) {
        if (inputArgs == null) {
            inputArgs = new ArrayList<VirtualObject>();
        }
        inputArgs.add(new VirtualObject(obj));

        return this;
    }

    /**
     * 添加一个参数
     *
     * @param obj  参数对象
     * @param desc 如果是从base获取，则需要设置desc
     * @return
     */
    public VirtualArgs addArg(Object obj, String desc) {
        if (inputArgs == null) {
            inputArgs = new ArrayList<VirtualObject>();
        }
        inputArgs.add(new VirtualObject(obj, desc));

        return this;
    }

    /**
     * 添加一个参数
     *
     * @param VirtualObject 参数对象
     * @return
     */
    public VirtualArgs addArg(VirtualObject vo) {
        if (inputArgs == null) {
            inputArgs = new ArrayList<VirtualObject>();
        }
        inputArgs.add(vo);

        return this;
    }

    public Object getArg(int i) {

        return inputArgs.get(i).getObject();

    }

    public List<String> getArgTypes() {
        List<String> argTypes = new ArrayList<String>();

        if (null != inputArgs) {
            for (VirtualObject virtualObject : inputArgs) {
                argTypes.add(virtualObject.getObjClass());
            }
        }
        return argTypes;
    }

    public List<Object> getInputArgs() {
        if (inputArgs == null) {
            return null;
        }
        List<Object> args = new ArrayList<Object>();
        for (VirtualObject virtualObject : inputArgs) {
            args.add(virtualObject.getObject());
        }
        return args;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setInputArgs(List<Object> inputArgs) {
        List args = new ArrayList();
        for (Iterator localIterator = inputArgs.iterator(); localIterator.hasNext(); ) {
            Object arg = localIterator.next();
            if (VirtualObject.class.isInstance(arg)) {
                args.add(arg);
            } else {
                args.add(new VirtualObject(arg));
            }
        }
        this.inputArgs = args;
    }

    //获取虚拟入参,名称不能是get,否则yaml有问题
    public List<VirtualObject> gainVirtualInputArgs() {
        if (inputArgs == null) {
            return null;
        }
        List<VirtualObject> args = new ArrayList<VirtualObject>();
        for (VirtualObject virtualObject : inputArgs) {
            args.add(virtualObject);
        }
        return args;
    }

    public List<VirtualObject> getVirtualObjects() {
        return inputArgs;
    }

    public void setArg(int index, Object obj) {
        if (inputArgs == null) {
            inputArgs = new ArrayList<VirtualObject>();
        }
        inputArgs.set(index, new VirtualObject(obj));

    }

    public void setArg(int index, Object obj, String desc) {
        VirtualObject voToAdd = new VirtualObject(obj, desc);
        setArg(index, voToAdd);

    }

    public void setVirtualInputArgs(List<VirtualObject> inputArgs) {
        List<VirtualObject> args = new ArrayList<VirtualObject>();
        for (Object arg : inputArgs) {
            args.add((VirtualObject) arg);
        }
        this.inputArgs = args;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setInputArgs(List<Object> inputArgs, List<String> desc) {
        List args = new ArrayList();
        if (inputArgs == null) {
            inputArgs = null;
            return;
        }
        for (int i = 0; i < inputArgs.size(); i++) {
            args.add(new VirtualObject(inputArgs.get(i), desc.get(i)));
        }
        this.inputArgs = args;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualArgs [inputArgs=" + inputArgs + "]";
    }

}
