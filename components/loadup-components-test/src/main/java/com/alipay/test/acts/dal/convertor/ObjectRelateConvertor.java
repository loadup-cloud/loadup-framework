/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.dal.convertor;

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

import com.alipay.test.acts.dal.dataobject.ObjectRelateDO;
import com.alipay.test.acts.dal.table.ObjectRelate;

/**
 * 对象关联关系模型转换器
 * 
 * @author hongling.xiang
 * @version $Id: ObjectRelateConvertor.java, v 0.1 2015年10月10日 下午11:36:50 hongling.xiang Exp $
 */
public class ObjectRelateConvertor {

    /**
     * 表模型转换为DO对象
     * 
     * @param objectRelate
     * @return
     */
    public static ObjectRelateDO convert2DO(ObjectRelate objectRelate) {

        if (null == objectRelate) {
            return null;
        }

        ObjectRelateDO objectRelateDO = new ObjectRelateDO();

        objectRelateDO.setDataDesc(objectRelate.getData_dsc());
        objectRelateDO.setGmtCreate(objectRelate.getGmt_create());
        objectRelateDO.setGmtModify(objectRelate.getGmt_modify());
        objectRelateDO.setId(objectRelate.getId());
        objectRelateDO.setMemo(objectRelate.getMemo());
        objectRelateDO.setModelData(objectRelate.getModel_data());
        objectRelateDO.setModelType(objectRelate.getModel_type());
        objectRelateDO.setModelObj(objectRelate.getModel_obj());
        objectRelateDO.setObjFlag(objectRelate.getObj_flag());
        objectRelateDO.setRelateSourceId(objectRelate.getRelate_source_id());
        objectRelateDO.setSourceData(objectRelate.getSource_data());
        objectRelateDO.setSystem(objectRelate.getSystem());

        return objectRelateDO;
    }

}
