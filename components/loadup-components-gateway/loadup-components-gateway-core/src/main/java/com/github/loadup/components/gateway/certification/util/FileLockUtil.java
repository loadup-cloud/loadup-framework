package com.github.loadup.components.gateway.certification.util;

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;

/**
 * 文件锁工具类
 */
public class FileLockUtil {

	/**
	 * 获取文件锁
	 *
	 * @throws Exception
	 */
	public static FileLock getFileLock(Object stream) throws Exception {
		FileLock fileLock = null;
		if (stream instanceof FileInputStream) {
			fileLock = ((FileInputStream) stream).getChannel().tryLock(0, Long.MAX_VALUE, true);
		} else if (stream instanceof FileOutputStream) {
			fileLock = ((FileOutputStream) stream).getChannel().tryLock();
		}
		return fileLock;
	}

	/**
	 * 单个文件锁的释放
	 *
	 * @throws Exception
	 */
	public static void release(FileLock fileLock) throws Exception {
		if (fileLock != null) {
			fileLock.release();
		}
	}

	/**
	 * 判断文件锁是否有效
	 */
	public static boolean isValidLock(FileLock fileLock) {
		boolean rtn = false;
		if (fileLock != null) {
			rtn = fileLock.isValid();
		}
		return rtn;
	}

}
