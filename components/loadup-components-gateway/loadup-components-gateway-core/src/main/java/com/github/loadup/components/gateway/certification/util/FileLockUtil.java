package com.github.loadup.components.gateway.certification.util;

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