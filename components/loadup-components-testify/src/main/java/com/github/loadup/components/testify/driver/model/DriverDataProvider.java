package com.github.loadup.components.testify.driver.model;

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

import com.github.loadup.components.testify.context.TestifySuiteContextHolder;
import com.github.loadup.components.testify.driver.AtsConfiguration;
import com.github.loadup.components.testify.driver.constants.AtsConstants;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 测试数据驱动
 *
 * @author fred.fanj
 * <p>
 * Exp $
 */
@Slf4j
public class DriverDataProvider implements Iterator<Object[]> {

    /**
     * 冒烟测试模式，执行指定的测试集
     */
    public static final String smokeFlag = AtsConfiguration.atsConfigMap
            .get(AtsConstants.SMOKE_TEST_FLAG);
    /**
     * 默认环境分割数据分割符
     * <p>
     * 可用于在csv中配置环境敏感的数据，如123|&|456，那么变量在读入到测试方法时可自动选择注入，devdb环境读入123, testdv环境读入456
     * </p>
     * <b>注：该功能仅在envSwitch为true时有效（默认为false，可调用{@link #(boolean)}设置）</b>
     */
    public static final String DEFAULT_ENV_SEPARATOR = "|&|";
    public int sum = 0;
    CSVReader reader = null;
    private Class<?>[] parameterTypes;
    private Converter[] parameterConverters;
    private SmokeMatcher smokeMatcher;
    // The latest row we returned
    private String[] last;

    /**
     * Basic constructor that will provide the data from the given file for the
     * given method *@throws IOException when file io fails
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public DriverDataProvider(Class<?> cls, Method method, String csvFilePath) {

        InputStream is = null;
        is = cls.getClassLoader().getResourceAsStream(csvFilePath);

        TestifySuiteContextHolder.get();

        try {
            smokeMatcher = new SmokeMatcher(smokeFlag);
            InputStreamReader isr = new InputStreamReader(is);
//            reader = new CSVReader(isr, ',', '\"', 0);// 跳过data title;
            reader = new CSVReader(isr);
            TestifySuiteContextHolder.get().setParameterKeyList(Arrays.asList(reader.readNext()));
            parameterTypes = method.getParameterTypes();
            int len = parameterTypes.length;
            parameterConverters = new Converter[len];
            for (int i = 0; i < len; i++) {
                parameterConverters[i] = ConvertUtils.lookup(parameterTypes[i]);
            }
        } catch (Exception e) {
            TestifyLogUtil.fail(log, cls.getName() + "." + method.getName()
                    + " TestData is not exist!");
        }

    }

    @Override
    public boolean hasNext() {
        try {
            if (reader == null) {
                return false;
            }
            // 读取一行驱动数据
            last = reader.readNext();
            // 判断读取的驱动数据是否有效,如果无效则继续读取
            while (last != null) {
                if (!smokeMatcher.match(last[0])) {
                    TestifyLogUtil.warn(log, "测试用例 [" + last[0] + "] 已经被过滤 [smoke_test=" + smokeFlag
                            + "]");
                    last = reader.readNext();
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "Read row data error!");
        }
        return last != null;
    }

    /**
     * Get the next line,or the current line if it's already there.
     *
     * @return the line.
     */
    private String[] getNextLine() {
        if (last == null) {
            try {
                last = reader.readNext();
            } catch (Exception ioe) {
                TestifyLogUtil.error(log, "get next line error!");
                throw new RuntimeException(ioe);
            }
        }
        return last;
    }

    /**
     * @return the Object[]representation of the next line
     */
    @Override
    public Object[] next() {
        String[] next;
        if (last != null) {
            next = last;
        } else {
            next = getNextLine();
        }
        last = null;
        Object[] args = parseLine(next);
        return args;
    }

    /**
     * @return the correctly parsed and wrapped values
     */
    private Object[] parseLine(String[] svals) {

        if (svals.length != parameterTypes.length) {
            TestifyLogUtil.fail(log, "驱动数据个数 [" + svals.length + "] 与参数个数 [" + parameterTypes.length
                    + "] 不相等 , " + svals[0]);
        }

        int len = svals.length;
        Object[] result = new Object[len];
        for (int i = 0; i < len; i++) {

            String curSval = svals[i];
            result[i] = parameterConverters[i].convert(parameterTypes[i], curSval);
//            log.debug(result[i]);
        }
        return result;
    }

    @Override
    public void remove() {
    }

    /**
     * 获取reader的句柄，直接对之进行操作
     *
     * @return
     */
    public CSVReader getReader() {
        return this.reader;
    }

}
