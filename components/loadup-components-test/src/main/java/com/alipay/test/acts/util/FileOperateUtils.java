package com.alipay.test.acts.util;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �ļ���������
 * @author tinghe
 * @version $Id: FileOperateUtils.java,v 0.1 2010-6-29 ����11:41:15 tinghe Exp $
 */
public class FileOperateUtils {

    /** Logger */
    protected static Log logger = LogFactory.getLog(FileOperateUtils.class);

    /**
     * �ݹ�����ļ�
     * @param findDir Ŀ¼
     * @param fileNameRegex �ļ���ƥ����ʽ, null:�������κ��ļ�
     * @param fileList �����ļ��б�
     */
    public static void findFileRecursive(final String findDir, final String fileNameRegex,
                                         List<File> fileList) {
        if (fileList == null) {
            return;
        }
        File file = new File(findDir);
        if (file.isFile()) {
            if (null == fileNameRegex || file.getName().matches(fileNameRegex)) {
                fileList.add(file);
            }
        } else if (file.isDirectory()) {
            File[] dirFiles = file.listFiles();
            for (File dirFile : dirFiles) {
                findFileRecursive(dirFile.getAbsolutePath(), fileNameRegex, fileList);
            }
        }
    }

    /**
     * �ݹ�����ļ�
     * @param findDir Ŀ¼
     * @param fileNameRegex �ļ���ƥ����ʽ, null:�������κ��ļ�
     * @param fileList �����ļ��б�
     */
    public static void findFolderRecursive(final String findDir, final String fileNameRegex,
                                           List<File> fileList) {
        if (fileList == null) {
            return;
        }
        File file = new File(findDir);
        if (file.isDirectory()) {
            if (null == fileNameRegex || file.getName().matches(fileNameRegex)) {
                fileList.add(file);
            } else {
                File[] dirFiles = file.listFiles();
                for (File dirFile : dirFiles) {
                    findFolderRecursive(dirFile.getAbsolutePath(), fileNameRegex, fileList);
                }
            }
        } else {
            return;
        }
    }

    /**
     * �����ļ�
     * @param fromFile
     * @return
     * @author mokong
     */
    public static boolean backupFile(File fromFile) {

        if (!fromFile.exists()) {
            return false;
        }

        String bakFileName = fromFile.getName() + ".bak";

        return renameFile(fromFile, bakFileName);

    }

    /**
     * �����ļ�����ɾ��ԭ�ļ�
     * @param fromFile
     * @return
     * @author mokong
     */
    public static boolean backupFileToDel(File fromFile) {

        if (!fromFile.exists()) {
            return false;
        }

        return backupFile(fromFile) && fromFile.delete();

    }

    /**
     * �������ļ�
     * ע�⣺��newName�Ѵ��ڣ���ֱ��ɾ��
     * 
     * @param fromFile-ԭ�ļ�
     * @param toFile-Ŀ���ļ�
     * @return
     * @author mokong
     */
    public static boolean renameFile(File fromFile, String newName) {

        // ���������ĺϷ��ԣ���newName�ļ��Ѵ��ڣ�ɾ��
        String orgiFilePath = fromFile.getParent();
        File newFile = new File(orgiFilePath + "/" + newName);

        if (newFile.exists() && newFile.delete()) {
            logger.error(newFile.getAbsolutePath() + " �Ѵ��ڲ�ɾ��ɹ���");
        }

        // ��ʽ����ԭʼ�ļ�
        if (fromFile.renameTo(newFile)) {
            logger.error(fromFile.getName() + "������Ϊ" + newFile.getName() + "�ɹ���");
            return true;
        } else {
            logger.error(fromFile.getName() + " ������Ϊ" + newFile.getName() + "ʧ�ܣ�");
            return false;
        }

    }

    /**
     * �����ļ���������;Ŀ���ļ������ڣ�ֱ��ɾ��
     * @param fromFile-ԭ�ļ�
     * @param toFile-Ŀ���ļ�
     * @return
     * @throws IOException 
     *  @author mokong
     */
    public static boolean copyFile(File fromFile, File toFile) throws IOException {

        if (toFile.exists() && toFile.delete()) {
            logger.error(toFile.getAbsolutePath() + " �Ѵ��ڲ�ɾ��ɹ���");
        }

        if (fromFile.exists()) {
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fromFile));
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(toFile));

            //����   
            int c;
            while ((c = bin.read()) != -1) {
                bout.write(c);

            }
            bin.close();
            bout.close();
            return true;
        } else {
            logger.error(fromFile.getAbsolutePath() + " �����ڣ�����ʧ�ܣ�");
            return false;
        }

    }
}
