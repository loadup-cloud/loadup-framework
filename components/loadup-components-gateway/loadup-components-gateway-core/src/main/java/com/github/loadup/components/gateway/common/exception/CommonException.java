// package com.github.loadup.components.gateway.common.exception;
//
/// *-
// * #%L
// * loadup-components-gateway-core
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
// import com.github.loadup.commons.result.ResultCode;
//
/// **
// * Base transaction
// */
// public class CommonException extends RuntimeException {
//
//    /**
//     * 异常代码
//     */
//    private final ResultCode resultCode;
//
//    /**
//     * 构造函数
//     */
//    public CommonException(ResultCode resultCode) {
//        this.resultCode = resultCode;
//    }
//
//    /**
//     * 构造函数
//     */
//    public CommonException(ResultCode resultCode, String message) {
//        super(message);
//        this.resultCode = resultCode;
//    }
//
//    /**
//     * 构造函数
//     */
//    public CommonException(ResultCode resultCode, Throwable e) {
//        super(e);
//        this.resultCode = resultCode;
//    }
//
//    /**
//     * 构造函数
//     */
//    public CommonException(ResultCode resultCode, String message, Throwable e) {
//        super(message, e);
//        this.resultCode = resultCode;
//    }
//
//    /**
//     * 获取异常代码
//     */
//    public ResultCode getErrorCode() {
//        return resultCode;
//    }
//
//    /**
//     * @see Throwable#getMessage()
//     */
//    @Override
//    public String getMessage() {
//        if (null == super.getMessage()) {
//            return this.resultCode.getResultMessage();
//        } else {
//            return super.getMessage();
//        }
//    }
//
//    /**
//     * @see Object#toString()
//     */
//    @Override
//    public String toString() {
//        StringBuilder retValue = new StringBuilder(" GatewayException[");
//        if (resultCode != null) {
//            retValue.append("code=").append(resultCode.getResultCode()).append(',');
//            retValue.append("description=").append(resultCode.getResultMessage()).append(',');
//        }
//        retValue.append("extraMessage=").append(getMessage());
//        retValue.append(']');
//        return retValue.toString();
//    }
// }
