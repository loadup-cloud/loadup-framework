/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.util;

/*-
 * #%L
 * loadup-components-gateway-facade
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

import java.io.*;

/**
 * 基于流的工具类. 部分方法移植自IBM developer works精彩文章, 参见package文档.
 */
public class IOUtils extends org.apache.commons.io.IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 从输入流读取内容, 写入到输出流中.  此方法使用大小为8192字节的默认的缓冲区.
     *
     * @throws IOException 输入输出异常
     */
    public static void io(InputStream in, OutputStream out) throws IOException {
        io(in, out, -1);
    }

    /**
     * 从输入流读取内容, 写入到输出流中.  使用指定大小的缓冲区.
     *
     * @throws IOException 输入输出异常
     */
    public static void io(InputStream in, OutputStream out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        byte[] buffer = new byte[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    /**
     * 从输入流读取内容, 写入到输出流中.  此方法使用大小为4096字符的默认的缓冲区.
     *
     * @throws IOException 输入输出异常
     */
    public static void io(Reader in, Writer out) throws IOException {
        io(in, out, -1);
    }

    /**
     * 从输入流读取内容, 写入到输出流中.  使用指定大小的缓冲区.
     *
     * @throws IOException 输入输出异常
     */
    public static void io(Reader in, Writer out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        }

        char[] buffer = new char[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    /**
     * 取得同步化的输出流.
     */
    public static OutputStream synchronizedOutputStream(OutputStream out) {
        return new SynchronizedOutputStream(out);
    }

    /**
     * 取得同步化的输出流.
     */
    public static OutputStream synchronizedOutputStream(OutputStream out, Object lock) {
        return new SynchronizedOutputStream(out, lock);
    }

    /**
     * 将指定输入流的所有文本全部读出到一个字符串中.
     *
     * @throws IOException 输入输出异常
     */
    public static String readText(InputStream in) throws IOException {
        return readText(in, null, -1);
    }

    /**
     * 将指定输入流的所有文本全部读出到一个字符串中.
     *
     * @throws IOException 输入输出异常
     */
    public static String readText(InputStream in, String encoding) throws IOException {
        return readText(in, encoding, -1);
    }

    /**
     * 将指定输入流的所有文本全部读出到一个字符串中.
     *
     * @throws IOException 输入输出异常
     */
    public static String readText(InputStream in, String encoding, int bufferSize) throws IOException {
        Reader reader = (encoding == null) ? new InputStreamReader(in) : new InputStreamReader(in, encoding);

        return readText(reader, bufferSize);
    }

    /**
     * 将指定<code>Reader</code>的所有文本全部读出到一个字符串中.
     *
     * @throws IOException 输入输出异常
     */
    public static String readText(Reader reader) throws IOException {
        return readText(reader, -1);
    }

    /**
     * 将指定<code>Reader</code>的所有文本全部读出到一个字符串中.
     *
     * @throws IOException 输入输出异常
     */
    public static String readText(Reader reader, int bufferSize) throws IOException {
        StringWriter writer = new StringWriter();

        io(reader, writer, bufferSize);
        return writer.toString();
    }

    /**
     * 同步化的输出流包裹器.
     */
    private static class SynchronizedOutputStream extends OutputStream {
        private OutputStream out;
        private Object lock;

        SynchronizedOutputStream(OutputStream out) {
            this(out, out);
        }

        SynchronizedOutputStream(OutputStream out, Object lock) {
            this.out = out;
            this.lock = lock;
        }

        public void write(int datum) throws IOException {
            synchronized (lock) {
                out.write(datum);
            }
        }

        public void write(byte[] data) throws IOException {
            synchronized (lock) {
                out.write(data);
            }
        }

        public void write(byte[] data, int offset, int length) throws IOException {
            synchronized (lock) {
                out.write(data, offset, length);
            }
        }

        public void flush() throws IOException {
            synchronized (lock) {
                out.flush();
            }
        }

        public void close() throws IOException {
            synchronized (lock) {
                out.close();
            }
        }
    }
}
