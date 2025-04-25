/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.data.biz;

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

import com.github.loadup.components.testify.data.MetaFieldType;
import com.github.loadup.components.testify.data.MetaInitItem;
import com.github.loadup.components.testify.data.MetaInitItemBuilder;
import com.github.loadup.components.testify.data.enums.MetaInitType;
import com.github.loadup.components.testify.util.FileUtil;
import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务模型元数据项构造器。
 *
 * 
 * 
 */
public class BizMetaInitItemBuilder implements MetaInitItemBuilder {

    //根据csv获取当前模型的名字,用来填充model_obj这一列
    private String readCurrentModelObj(String csvPath) {
        String CurrentQuafiedName = "";
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
            //这是header部分
            csvReader.readNext();
            String[] firstLine = csvReader.readNext();

            if (firstLine != null) {
                CurrentQuafiedName = firstLine[0];
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

        return CurrentQuafiedName;
    }

    /**
     * 文件结构
     * class,property,type,rule,flag,description
     * ,productCode,java.lang.String,rule,Y,01071000100000000001
     *
     * @see MetaInitItemBuilder#build(String)
     */
    @Override
    public List<MetaInitItem> build(String system, String projectPath) {

        File dir = FileUtil.findObjModelPath(new File(projectPath));
        List<MetaInitItem> initItems = new ArrayList<MetaInitItem>();

        @SuppressWarnings("unchecked")
        List<File> allCSV = (List<File>) FileUtils.listFiles(dir, new String[]{"csv"}, true);
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
                    MetaInitItem initItem = new MetaInitItem(system, objName, line[1]);
                    initItem.setInitType(MetaInitType.BIZ);
                    initItem.setFieldType(new MetaFieldType(line[2]));
                    initItems.add(initItem);
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
