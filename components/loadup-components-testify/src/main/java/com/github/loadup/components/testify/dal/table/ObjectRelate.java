/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.dal.table;

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

import java.util.Date;

/**
 * 数据库表字段对象
 *
 *
 *
 */
public class ObjectRelate {

    private String id;

    private String system;

    private String model_obj;

    private String model_data;

    private String model_type;

    private String data_dsc;

    private String obj_flag;

    private String relate_source_id;

    private String source_data;

    private String memo;

    private Date gmt_create;

    private Date gmt_modify;

    /**
     *
     *
     * @return property value of id
     */
    public String getId() {
        return id;
    }

    /**
     *
     *
     * @param id value to be assigned to property id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     *
     * @return property value of system
     */
    public String getSystem() {
        return system;
    }

    /**
     *
     *
     * @param system value to be assigned to property system
     */
    public void setSystem(String system) {
        this.system = system;
    }

    /**
     *
     *
     * @return property value of model_type
     */
    public String getModel_type() {
        return model_type;
    }

    /**
     *
     *
     * @param model_type value to be assigned to property model_type
     */
    public void setModel_type(String model_type) {
        this.model_type = model_type;
    }

    /**
     *
     *
     * @return property value of model_obj
     */
    public String getModel_obj() {
        return model_obj;
    }

    /**
     *
     *
     * @param model_obj value to be assigned to property model_obj
     */
    public void setModel_obj(String model_obj) {
        this.model_obj = model_obj;
    }

    /**
     *
     *
     * @return property value of model_data
     */
    public String getModel_data() {
        return model_data;
    }

    /**
     *
     *
     * @param model_data value to be assigned to property model_data
     */
    public void setModel_data(String model_data) {
        this.model_data = model_data;
    }

    /**
     *
     *
     * @return property value of data_dsc
     */
    public String getData_dsc() {
        return data_dsc;
    }

    /**
     *
     *
     * @param data_dsc value to be assigned to property data_dsc
     */
    public void setData_dsc(String data_dsc) {
        this.data_dsc = data_dsc;
    }

    /**
     *
     *
     * @return property value of obj_flag
     */
    public String getObj_flag() {
        return obj_flag;
    }

    /**
     *
     *
     * @param obj_flag value to be assigned to property obj_flag
     */
    public void setObj_flag(String obj_flag) {
        this.obj_flag = obj_flag;
    }

    /**
     *
     *
     * @return property value of relate_source_id
     */
    public String getRelate_source_id() {
        return relate_source_id;
    }

    /**
     *
     *
     * @param relate_source_id value to be assigned to property relate_source_id
     */
    public void setRelate_source_id(String relate_source_id) {
        this.relate_source_id = relate_source_id;
    }

    /**
     *
     *
     * @return property value of source_data
     */
    public String getSource_data() {
        return source_data;
    }

    /**
     *
     *
     * @param source_data value to be assigned to property source_data
     */
    public void setSource_data(String source_data) {
        this.source_data = source_data;
    }

    /**
     *
     *
     * @return property value of memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     *
     *
     * @param memo value to be assigned to property memo
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     *
     *
     * @return property value of gmt_create
     */
    public Date getGmt_create() {
        return gmt_create;
    }

    /**
     *
     *
     * @param gmt_create value to be assigned to property gmt_create
     */
    public void setGmt_create(Date gmt_create) {
        this.gmt_create = gmt_create;
    }

    /**
     *
     *
     * @return property value of gmt_modify
     */
    public Date getGmt_modify() {
        return gmt_modify;
    }

    /**
     *
     *
     * @param gmt_modify value to be assigned to property gmt_modify
     */
    public void setGmt_modify(Date gmt_modify) {
        this.gmt_modify = gmt_modify;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ObjectRelate [id="
                + id
                + ", system="
                + system
                + ", model_obj="
                + model_obj
                + ", model_data="
                + model_data
                + ", model_type="
                + model_type
                + ", data_dsc="
                + data_dsc
                + ", obj_flag="
                + obj_flag
                + ", relate_source_id="
                + relate_source_id
                + ", source_data="
                + source_data
                + ", memo="
                + memo
                + ", gmt_create="
                + gmt_create
                + ", gmt_modify="
                + gmt_modify
                + "]";
    }
}
