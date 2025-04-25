/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.object.processor;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.StringUtils;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;

/***
 * flag 读取器
 *
 * 
 *
 */
@Slf4j
public class FlagProcessor {


    private final int CLASSNAMECOL = 0;
    private final int PROPERTYNAMECOL = 1;
    private final int FLAGVALUECOL = 4;

    public FlagProcessor() {

    }

    /***
     * 对外组装的方法
     *
     * @param csvPath
     * @param encoding
     * @return
     */
    public Map<String, Map<String, String>> genFlag(String csvPath, String encoding) {

        Map<String, Map<String, String>> map = processCsvFolder(csvPath, encoding);
        return map;
    }

    @SuppressWarnings({"rawtypes"})
    private Map<String, Map<String, String>> processCsvFolder(String csvPath, String encoding)
            throws TestifyException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        File csvFile = new File(csvPath);

        File folder = csvFile.getParentFile();

        Collection allFiles = FileUtils.listFiles(folder, new String[]{"csv"}, true);
        Iterator iterator = allFiles.iterator();

        while (iterator.hasNext()) {
            File currentFile = (File) iterator.next();
            if (isGeneric(currentFile)) {
                continue;
            }
            Map<String, Map<String, String>> oneMap = processCsvFile(currentFile.getAbsolutePath(),
                    encoding);
            result.putAll(oneMap);
        }

        return result;

    }

    /***
     * 对于泛型不生成
     *
     * @param file
     * @return
     */
    private boolean isGeneric(File file) {

        String fileName = file.getName();
        if (StringUtils.contains(fileName, "_")) {
            return true;
        }
        return false;

    }

    @SuppressWarnings({"rawtypes"})
    private Map<String, Map<String, String>> processCsvFile(String csvPath, String encoding) {

        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        //1. 加载CSV数据
        List tableList = CSVHelper.readFromCsv(csvPath, encoding);
        if (tableList == null || tableList.size() == 0)
            TestifyLogUtil.fail(log, csvPath + "文件内容为空");
        if (tableList.size() < 2) {
            throw new TestifyException("当前的CSV没有内容或者内容不全,文件名为" + csvPath);
        }

        String className = ((String[]) tableList.get(1))[CLASSNAMECOL];
        Map<String, String> value = new HashMap<String, String>();
        result.put(className, value);
        for (int i = 2; i < tableList.size(); i++) {
            String[] row = (String[]) tableList.get(i);
            String fieldName = row[PROPERTYNAMECOL];
            String flagCode = row[FLAGVALUECOL];
            result.get(className).put(fieldName, flagCode);

        }
        return result;

    }

}
