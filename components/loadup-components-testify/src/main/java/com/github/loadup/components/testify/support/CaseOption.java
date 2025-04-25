/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.support;

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
 * 测试选项，目前有控制用例是否执行的属性isRun
 *
 * @author keke.wk
 *
 */
public class CaseOption {

    /**
     * 用例是否执行
     */
    private boolean isRun = true;

    /**
     * 生成摘要日志。
     *
     * @return
     */
    public String toDigest() {
        StringBuffer digest = new StringBuffer();
        digest.append("(");
        digest.append(isRun ? "Y" : "N");
        digest.append(")");
        return digest.toString();
    }

    // ~~~ bean方法

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("isRun:");
        str.append(isRun ? "Y" : "N");
        // 多个配置项以"|"连接

        return str.toString();
    }

    /**
     *
     *
     * @return property value of isRun
     */
    public boolean isRun() {
        return isRun;
    }

    /**
     *
     *
     * @param isRun value to be assigned to property isRun
     */
    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }

}
