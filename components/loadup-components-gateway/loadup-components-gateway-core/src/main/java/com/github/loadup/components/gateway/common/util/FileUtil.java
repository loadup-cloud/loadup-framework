package com.github.loadup.components.gateway.common.util;

/*-
 * #%L
 * loadup-components-gateway-core
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

import java.io.IOException;
import java.io.InputStream;

/**
 * 提供文件交换中与文件相关操作的帮助。
 *
 * <p>
 * 文件路径表达式：
 * 指路径是随日期或其它因素而变化，如有一个路径表达式为 /home/download/${MM}/${DD}，
 * 则在20120812这一天，它的实际路径是/home/download/08/12。
 * </p>
 */
public class FileUtil {

    /**
     * New method, to get file content
     *
     * @throws IOException
     */
    public static String getFileContent(String filePath, Object obj) throws IOException {
        InputStream inputFileStream = null;
        try {
            inputFileStream = obj.getClass().getClassLoader().getResourceAsStream(filePath);
            byte[] fileByte = new byte[inputFileStream.available()];
            inputFileStream.read(fileByte);
            return new String(fileByte, "UTF-8");
        } finally {
            if (null != inputFileStream) {
                inputFileStream.close();
            }
        }
    }
}
