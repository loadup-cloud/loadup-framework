/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.lothar;

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
 *LOTHAR校验对象
 * @author tianzhu.wtzh
 * @version $Id: ActsLotharMockCheckModel.java, v 0.1 2019年10月29日 上午11:16 tianzhu.wtzh Exp $
 */
public class ActsLotharMockCheckModel {

    /**Check类名*/
    public String className ="";

    /**Check方法名*/
    public String methodName ="";

    /**入参位置*/
    public Integer index;

    /**对象属性名*/
    public String fieldName ="";

    /**
     * checkInfo格式:类名#方法名#入参位置#属性名
     * @param checkInfo
     */
    public ActsLotharMockCheckModel(String checkInfo){

        if(checkInfo != null){
            String[] info = checkInfo.split("#");
            if(info.length ==4){
                this.className = info[0];
                this.methodName = info[1];
                this.index = Integer.valueOf(info[2]);
                this.fieldName = info[3];
            }else if(info.length == 3){
                this.className = info[0];
                this.methodName = info[1];
                this.index = Integer.valueOf(info[2]);
            }else if(info.length == 2){
                this.className = info[0];
                this.methodName = info[1];
            }else if(info.length == 1){
                this.className = info[0];
            }
        }
    }

    /**
     * Getter method for property <tt>className</tt>.
     *
     * @return property value of className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Setter method for property <tt>className</tt>.
     *
     * @param className  value to be assigned to property className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter method for property <tt>methodName</tt>.
     *
     * @return property value of methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Setter method for property <tt>methodName</tt>.
     *
     * @param methodName  value to be assigned to property methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Getter method for property <tt>index</tt>.
     *
     * @return property value of index
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Setter method for property <tt>index</tt>.
     *
     * @param index  value to be assigned to property index
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Getter method for property <tt>fieldName</tt>.
     *
     * @return property value of fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Setter method for property <tt>fieldName</tt>.
     *
     * @param fieldName  value to be assigned to property fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
