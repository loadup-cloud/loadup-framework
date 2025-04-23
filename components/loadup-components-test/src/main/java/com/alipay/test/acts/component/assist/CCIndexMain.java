/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */

package com.alipay.test.acts.component.assist;

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

/**
 * index生成工具的入口
 * 
 * @author shuhe.th
 * @version $Id: CCIndexMain.java, v 0.1 2015-6-17 下午3:13:58 shuhe.th Exp $
 */
public class CCIndexMain {
    public static void main(String[] args) throws Throwable {

        String groupId = args[0];
        String[] fields = groupId.split("\\.");
        String appName = fields[fields.length - 1];

        String curpath = args[1];
        String prefix = curpath.substring(0, curpath.lastIndexOf("src"));
        String jsonpath = (prefix + "components\\" + appName + ".json").replace("\\",
            File.separator);
        String classpath = (prefix + "target\\test-classes\\").replace("\\", File.separator);
        String[] pckgs = { groupId + ".test.cccheck", groupId + ".test.ccclear",
                groupId + ".test.ccmock", groupId + ".test.ccprepare" };
        String outpath = (prefix + "src\\test\\java\\" + groupId.replace(".", "\\") + "\\test\\")
            .replace("\\", File.separator);

        CCIndexBuilder builder = new CCIndexBuilder(classpath, pckgs, outpath, jsonpath);
        builder.run();

    }
}
