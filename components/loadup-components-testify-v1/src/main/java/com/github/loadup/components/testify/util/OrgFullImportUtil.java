/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.util;

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

import com.github.loadup.components.testify.dal.convertor.ObjectRelateConvertor;
import com.github.loadup.components.testify.dal.convertor.OrgDbConvertor;
import com.github.loadup.components.testify.dal.daointerface.ObjRelateDAO;
import com.github.loadup.components.testify.dal.daointerface.OrgDbDAO;
import com.github.loadup.components.testify.dal.table.ObjectRelate;
import com.github.loadup.components.testify.dal.table.OrgDb;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

/**
 * 导入工具
 *
 *
 *
 */
public class OrgFullImportUtil {

    List<ObjectRelate> objRelateList = new ArrayList<ObjectRelate>();
    List<OrgDb> objDbList = new ArrayList<OrgDb>();

    public void importOrgRelate(String appName, String csvPathFolder) {
        parseSourceData(appName, csvPathFolder);
        OrgDbDAO orgDbDAO = new OrgDbDAO();
        for (OrgDb orgDb : objDbList) {
            int i = orgDbDAO.insert(OrgDbConvertor.convert2DO(orgDb));
            Assert.assertEquals(1, i);
            System.out.println(orgDb);
        }
        ObjRelateDAO objRelateDAO = new ObjRelateDAO();
        for (ObjectRelate objectRelate : objRelateList) {
            int i = objRelateDAO.insert(ObjectRelateConvertor.convert2DO(objectRelate));
            Assert.assertEquals(1, i);
            System.out.println(objectRelate);
        }
    }

    /***
     * 获取字符串最后一个字符串,比如,a.b.c,指定分隔符.,则返回c
     *
     * @param str
     * @param separator
     * @return
     */
    private String getLastString(String str, String separator) {
        String[] strArrs = StringUtils.split(str, separator);
        String result = strArrs[strArrs.length - 1];
        return result;
    }

    // 根据csv获取当前模型的名字,用来填充model_obj这一列
    private String readCurrentModelObj(String csvPath) {
        String currentModelObj = "";
        File f = new File(csvPath);
        InputStream in = null;
        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Reader isr = new InputStreamReader(in);
        CSVReader csvReader = new CSVReader(isr);

        try {
            // 这是header部分
            csvReader.readNext();
            String[] firstLine = csvReader.readNext();

            String CurrentQuafiedName = firstLine[0];
            currentModelObj = getLastString(CurrentQuafiedName, ".");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return currentModelObj;
    }

    /*
     * 返回java内置基本类型的对象
     */
    protected boolean isPrimitive(String primitiveType) {

        boolean result = false;

        if (StringUtils.equals("int", primitiveType) || StringUtils.equals("java.lang.Integer", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("float", primitiveType) || StringUtils.equals("java.lang.Float", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("String", primitiveType)
                || StringUtils.equals("java.lang.String", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("double", primitiveType)
                || StringUtils.equals("java.lang.Double", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("long", primitiveType) || StringUtils.equals("java.lang.Long", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("short", primitiveType) || StringUtils.equals("java.lang.Short", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("byte", primitiveType) || StringUtils.equals("java.lang.Byte", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("boolean", primitiveType)
                || StringUtils.equals("java.lang.Boolean", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("char", primitiveType)
                || StringUtils.equals("java.lang.Character", primitiveType)) {
            result = true;
        } else if (StringUtils.equals("Date", primitiveType) || StringUtils.equals("java.util.Date", primitiveType)) {
            result = true;
        } else {
        }

        return result;
    }

    private void parseSourceData(String appName, String csvPathFolder) {

        File dir = new File(csvPathFolder);
        @SuppressWarnings("unchecked")
        List<File> allCSV = (List<File>) FileUtils.listFiles(dir, new String[] {"csv"}, true);
        for (File f : allCSV) {
            InputStream in = null;
            try {
                in = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Reader isr = new InputStreamReader(in);
            CSVReader csvReader = new CSVReader(isr);
            String[] line;
            try {

                String objName = readCurrentModelObj(f.getAbsolutePath());
                line = csvReader.readNext();
                // 跳过行首
                line = csvReader.readNext();
                while (line != null) {

                    // 先构建OrgDb
                    OrgDb orgDb = new OrgDb();
                    String sourceData = line[1];
                    orgDb.setSource_data(sourceData);
                    orgDb.setSystem(appName);
                    orgDb.setGmt_create(new Date());
                    orgDb.setGmt_modify(new Date());
                    String sourceId = appName + "_" + sourceData;
                    orgDb.setSource_id(sourceId);
                    ObjectRelate objectRelate = new ObjectRelate();
                    objectRelate.setGmt_create(new Date());
                    objectRelate.setGmt_modify(new Date());
                    objectRelate.setSystem(appName);
                    objectRelate.setModel_obj(objName);
                    objectRelate.setModel_data(sourceData);
                    String fullType = line[2];
                    fullType = getLastString(fullType, ".");
                    objectRelate.setModel_type(fullType);
                    String id = appName + "_" + objName + "_" + sourceData;
                    objectRelate.setId(id);
                    // 如果fullType是简单类型.则设置flag为obj,并且source_data和relate_source_id设置NA
                    if (isPrimitive(line[2])) {
                        objectRelate.setSource_data(line[1]);
                        objectRelate.setRelate_source_id(orgDb.getSource_id());
                        objectRelate.setObj_flag(line[3]);
                    } else {
                        objectRelate.setSource_data("NA");
                        objectRelate.setRelate_source_id("NA");
                        objectRelate.setObj_flag("obj");
                    }
                    objRelateList.add(objectRelate);
                    objDbList.add(orgDb);
                    line = csvReader.readNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    csvReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
