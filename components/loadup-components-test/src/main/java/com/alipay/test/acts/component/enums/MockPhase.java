/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.enums;

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
 * 基于行为的mock，一般包含三个阶段：mock、verify和unmock。
 * <p>
 * 当方法指定仓储类型为{@link com.ipay.itest.common.enums.RepositoryType#MOCK Mock} 时，
 * 需要同时指定<code>MockPhase</code>，以标记当前所属阶段。
 * <p>
 * 三个阶段共用一个mock ID，以保证运行时完整执行。比如：
 * <p><blockquote><pre>
 *      {@code @CCMock(id="xxxx",phase=MockPhase.Mock)}
 *      void xxxxMock() {
 *      }
 *      
 *      {@code @CCMock(id="xxxx",phase=MockPhase.Unmock)}
 *      void xxxxUnmock() {
 *      } 
 *          
 * </pre></blockquote>

 * 
 * 
 * @author dasong.jds
 * @version $Id: MockPhase.java, v 0.1 2015年4月23日 下午5:07:03 dasong.jds Exp $
 */
public enum MockPhase {

    /** mock阶段，初始mock对象，并定义行为 */
    Mock,

    /** 验证阶段，验证mock对象的调用是否符合预期 */
    Verify,

    /** unmock阶段，还原mock对象 */
    Unmock;

}
