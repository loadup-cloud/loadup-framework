/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data;

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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 原子项，定义数据生成规则。
 * 
 * @author dasong.jds
 * @version $Id: MetaItem.java, v 0.1 2015年10月12日 下午4:18:57 dasong.jds Exp $
 */
public class MetaItem {

    /** 系统名 */
    private final String system;

    /** 统一标识，全局唯一 */
    private final String id;

    /** 关联的关键字，方便匹配 */
    private List<String> keywords = new ArrayList<String>();

    /** 数据规则 */
    private String       dataRule;

    /**
     * @param system
     * @param id
     */
    public MetaItem(String system, String id) {
        super();
        this.system = system;
        this.id = id;
    }

    /**
     * 增加关键字。
     * 
     * @param keyword
     */
    public void addKeyword(String keyword) {
        keywords.add(keyword);
        if (keyword.indexOf("_") >= 0) {
            keywords.add(StringUtils.replace(keyword, "_", ""));
        }
    }

    /**
     * Getter method for property <tt>id</tt>.
     * 
     * @return property value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter method for property <tt>keywords</tt>.
     * 
     * @return property value of keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Setter method for property <tt>keywords</tt>.
     * 
     * @param keywords value to be assigned to property keywords
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Getter method for property <tt>dataRule</tt>.
     * 
     * @return property value of dataRule
     */
    public String getDataRule() {
        return dataRule;
    }

    /**
     * Setter method for property <tt>dataRule</tt>.
     * 
     * @param dataRule value to be assigned to property dataRule
     */
    public void setDataRule(String dataRule) {
        this.dataRule = dataRule;
    }

    /**
     * Getter method for property <tt>system</tt>.
     * 
     * @return property value of system
     */
    public String getSystem() {
        return system;
    }

    /** 
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((system == null) ? 0 : system.hashCode());
        return result;
    }

    /** 
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetaItem other = (MetaItem) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (system == null) {
            if (other.system != null) {
                return false;
            }
        } else if (!system.equals(other.system)) {
            return false;
        }
        return true;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MetaItem [system=");
        builder.append(system);
        builder.append(",id=");
        builder.append(id);
        builder.append(",keywords=");
        builder.append(keywords);
        builder.append(",dataRule=");
        builder.append(dataRule);
        builder.append("]");
        return builder.toString();
    }

}
