package com.github.loadup.components.gateway.common.util;

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