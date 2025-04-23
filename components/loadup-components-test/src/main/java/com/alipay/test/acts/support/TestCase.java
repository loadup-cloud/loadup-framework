/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.support;

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

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.manage.TestLogger;
import com.alipay.test.acts.support.enums.CaseCategory;
import com.alipay.test.acts.support.enums.CaseLevel;

/**
 * 测试用例。
 * <p>
 * 用于用例定义、用例管理、用例治理等，统一标准规范。
 * <p>
 * 用例ID规范:
 *      <table border="1">
 *          <tr>
 *           <td><b>组成部分</b></td><td>用例分类</td><td>分<br>隔<br>符</td><td>用例等级</td><td>分<br>隔<br>符<td>业务描述</td><td>分<br>隔<br>符</td><td>修饰性描述</td><td>分<br>隔<br>符</td><td>序列号</td>
 *          </tr>
 *          <tr>
 *           <td><b>示例</b></td><td>NM</td><td>_</td><td>H</td><td>_</td><td>TRADE_CREATE</td><td>_</td><td>ESCROW</td><td>_</td><td>001</td>
 *          </tr>
 *          <tr>
 *           <td><b>说明</b></td><td>用例分类简码<br>NM/FE/PE/SE</td><td>_</td><td>用例等级简码<br>H/M/L</td><td>_</td><td>一般与服务一致</td><td>_</td><td>一般基于业务参数</td><td>_</td><td>3位序列号</td>
 *          </tr>
 *      </table>
 * 
 * 
 * @author dasong.jds
 * @version $Id: TestCase.java, v 0.1 2015年5月27日 下午1:51:13 dasong.jds Exp $
 */
public class TestCase {

    /** 用例ID */
    private final String       caseId;

    /** 分类 */
    private final CaseCategory category;

    /** 等级 */
    private final CaseLevel    level;

    /**用例选项信息*/
    private final CaseOption   option;

    /** 用例描述信息 */
    private String             caseDescription;

    /**
     * 构造测试用例。
     * 
     * @param caseId
     * @param description
     */
    public TestCase(String caseId, String description) {
        this(caseId, new CaseOption(), description);
    }

    /**
     * 构造测试用例。
     * 
     * @param caseId
     * @param testOption
     * @param description
     */
    public TestCase(String caseId, CaseOption option, String description) {
        super();
        this.caseId = caseId;
        this.caseDescription = description;
        this.option = option;

        CaseProperties props = parseCategoryAndLevelByCaseId(caseId);
        this.category = props.category;
        this.level = props.level;
    }

    /**
     * 生成摘要信息。
     * 
     * @return
     */
    public String toDigest() {
        StringBuilder digest = new StringBuilder();
        digest.append("[");
        digest.append(caseId);
        if (this.option != null) {
            digest.append(this.option.toDigest());
        }
        digest.append("]");
        return digest.toString();

    }

    /**
     * 获取用例表达式
     * 
     * @return
     */
    public String getCaseExpr() {

        String expr = caseId;

        if (option != null) {
            expr = expr + ";" + option;
        }

        if (!StringUtils.isBlank(caseDescription)) {
            expr = expr + ";" + caseDescription;
        }

        return expr;
    }

    // ~~~ 私有实现
    /**
     * 通过caseId设置分类信息和等级信息
     * 
     * @param caseId
     */
    private CaseProperties parseCategoryAndLevelByCaseId(String caseId) {
        CaseProperties props = new CaseProperties();

        String[] caseIdInfo = caseId.split("_");
        if (caseIdInfo.length < 3) {
            TestLogger.getLogger().warn("test case id illegal, pattern is NM_H_XXX_XXX]");
            return props;
        }

        CaseCategory category = CaseCategory.getCaseCategoryByCode(caseIdInfo[0]);
        if (category == null) {
            TestLogger.getLogger().warn(
                "case category define error, value must be NM, FE, PE or SE");
        }
        CaseLevel level = CaseLevel.getLevelByCode(caseIdInfo[1]);
        if (level == null) {
            TestLogger.getLogger().warn("case level define error, value must be H, M or L");
        }

        props.category = category;
        props.level = level;

        return props;
    }

    // ~~~ bean方法
    /**
     * Getter method for property <tt>caseId</tt>.
     * 
     * @return property value of caseId
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * Getter method for property <tt>caseDescription</tt>.
     * 
     * @return property value of caseDescription
     */
    public String getCaseDescription() {
        return caseDescription;
    }

    /**
     * Setter method for property <tt>caseDescription</tt>.
     * 
     * @param caseDescription value to be assigned to property caseDescription
     */
    public void setCaseDescription(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    /**
     * Getter method for property <tt>level</tt>.
     * 
     * @return property value of level
     */
    public CaseLevel getLevel() {
        return level;
    }

    /**
     * Getter method for property <tt>option</tt>.
     * 
     * @return property value of option
     */
    public CaseOption getOption() {
        return option;
    }

    /**
     * Getter method for property <tt>category</tt>.
     * 
     * @return property value of category
     */
    public CaseCategory getCategory() {
        return category;
    }

    /**
     * 用例属性。
     */
    static class CaseProperties {

        private CaseLevel    level;

        private CaseCategory category;
    }
}
