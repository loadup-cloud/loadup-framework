package com.github.loadup.components.testify.util;

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

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

/**
 *
 */
@Slf4j
public class FileOperateUtils {

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

    public static boolean backupFile(File fromFile) {

        if (!fromFile.exists()) {
            return false;
        }

        String bakFileName = fromFile.getName() + ".bak";

        return renameFile(fromFile, bakFileName);

    }


    public static boolean backupFileToDel(File fromFile) {

        if (!fromFile.exists()) {
            return false;
        }

        return backupFile(fromFile) && fromFile.delete();

    }


    public static boolean renameFile(File fromFile, String newName) {

        String orgiFilePath = fromFile.getParent();
        File newFile = new File(orgiFilePath + "/" + newName);

        if (newFile.exists() && newFile.delete()) {
            log.error(newFile.getAbsolutePath() + "");
        }

        if (fromFile.renameTo(newFile)) {
            log.error(fromFile.getName() + "" + newFile.getName() + "");
            return true;
        } else {
            log.error(fromFile.getName() + "" + newFile.getName() + "");
            return false;
        }

    }

    /**
     * @return
     * @throws IOException
     */
    public static boolean copyFile(File fromFile, File toFile) throws IOException {

        if (toFile.exists() && toFile.delete()) {
            log.error(toFile.getAbsolutePath() + "");
        }

        if (fromFile.exists()) {
            BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fromFile));
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(toFile));

            int c;
            while ((c = bin.read()) != -1) {
                bout.write(c);

            }
            bin.close();
            bout.close();
            return true;
        } else {
            log.error(fromFile.getAbsolutePath() + "");
            return false;
        }

    }
}
