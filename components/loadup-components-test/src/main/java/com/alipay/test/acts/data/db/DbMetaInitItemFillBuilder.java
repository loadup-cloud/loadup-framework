/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.data.db;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.opencsv.CSVReader;

import com.alipay.test.acts.dal.table.CsvFillObject;
import com.alipay.test.acts.util.FileUtil;

/**
 * 业务模型元数据项构造器。
 * 
 * @author dasong.jds
 * @version $Id: BizMetaInitItemBuilder.java, v 0.1 2015年10月12日 下午3:40:06 dasong.jds Exp $
 */
public class DbMetaInitItemFillBuilder {
    //根据csv获取当前模型的名字,用来填充model_obj这一列
    private String readCurrentModelObj(File csvPath) {

        String name = csvPath.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    public List<CsvFillObject> build(String system, String projectPath) {

        File dir = FileUtil.findDbModelPath(new File(projectPath));

        List<CsvFillObject> initItems = new ArrayList<CsvFillObject>();

        @SuppressWarnings("unchecked")
        List<File> allCSV = (List<File>) FileUtils.listFiles(dir, new String[] { "csv" }, true);
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

                String objName = readCurrentModelObj(f);
                line = csvReader.readNext();
                // 跳过行首
                line = csvReader.readNext();
                while (line != null) {
                    CsvFillObject fillObject = new CsvFillObject();
                    fillObject.setSystem(system);
                    fillObject.setModel_obj(objName);
                    fillObject.setModel_data(line[0]);
                    fillObject.setData_rule(line[3]);
                    initItems.add(fillObject);
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
        return initItems;
    }
}
