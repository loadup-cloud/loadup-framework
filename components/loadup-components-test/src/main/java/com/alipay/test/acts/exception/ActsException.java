/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.exception;

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
 * 测试异常基类，框架中异常声明必须继承此接口。
 * 
 * @author dasong.jds
 * @version $Id: ActsException.java, v 0.1 2015年4月19日 下午9:07:58 dasong.jds Exp $
 */
public class ActsException extends RuntimeException {

    /** 序列ID */
    private static final long serialVersionUID = 4670114874512893276L;

    /**
     * 构造一个<code>ActsException</code>对象。
     */
    public ActsException() {
        super();
    }

    /**
     * 构造一个<code>ActsException</code>对象。
     * 
     * @param message           异常描述
     */
    public ActsException(String message) {
        super(message);
    }

    /**
     * 构造一个<code>ActsException</code>对象。
     * 
     * @param cause             异常原因
     */
    public ActsException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个<code>ActsException</code>对象。
     * 
     * @param message           异常描述
     * @param cause             异常原因
     */
    public ActsException(String message, Throwable cause) {
        super(message, cause);
    }

}
