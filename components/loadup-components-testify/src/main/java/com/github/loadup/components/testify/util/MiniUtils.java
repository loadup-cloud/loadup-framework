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

import com.github.loadup.components.testify.constant.TestifyConstants;
import com.github.loadup.components.testify.driver.TestifyConfiguration;
import com.github.loadup.components.testify.driver.constants.AtsConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * ��Сҵ��ԪUtils
 *
 * 
 *
 */
public class MiniUtils {

    /**
     * mini-bundle config
     */
    public static String miniBundleConfig = TestifyConfiguration.getInstance().getConfigMap()
            .get(TestifyConstants.MINI_BUNDLE);

    /**
     * open_mini-bundle config
     */
    public static String openMiniBundleConfig = TestifyConfiguration.getInstance().getConfigMap()
            .get(TestifyConstants.OPEN_MINI_BUNDLE);

    /**
     * mini-replace-xml config
     */
    public static String miniReplaceXmlConfig = TestifyConfiguration.getInstance().getConfigMap()
            .get(AtsConstants.MINI_EXCLUDE_XML);

    /**
     * @return
     */
    public static boolean isLoadMiniBundles() {

        return StringUtils.isNotBlank(miniBundleConfig);

    }

    /**
     * 是否开启动态mini-bundle
     *
     * @return
     */
    public static boolean isOpenMiniBundles() {

        if (StringUtils.isNotBlank(openMiniBundleConfig)) {
            return Boolean.valueOf(openMiniBundleConfig).booleanValue();
        } else {
            return false;
        }

    }

    /**
     * @param excludedBundles
     */
    public static String[] loadMiniBundles() {

        String[] excludedBundles = new String[]{};

        if (StringUtils.isNotBlank(miniBundleConfig)) {

            // Get mini-bundle flag
            List<?> miniBundlesFlagList = CollectionConvertUtil.arrayConvertToList(miniBundleConfig
                    .split(","));

            /*
             * basis of miniBundlesFlagList,read mini-bundle-config.properties
             * info, to get Require Bundle info
             */
            Properties miniBundleConfig = PropertyFileUtil
                    .readProperties("/config/miniConf/mini-bundle-config.properties");

            List<Object> requiredBundleList = new ArrayList<Object>();

            for (Entry<Object, Object> entry : miniBundleConfig.entrySet()) {
                String key = (String) entry.getKey();
                if (miniBundlesFlagList.contains(key)) {
                    requiredBundleList.addAll(CollectionConvertUtil
                            .arrayConvertToList(miniBundleConfig.getProperty(key).split(",")));

                }

            }

            // excludedBundles ͨ��ȫ����ȥrequiredBundleList��ȡ
            HashSet<String> requiredBundlesSet = new HashSet<String>();
            HashSet<String> excludedBundlesSet = new HashSet<String>();

            for (int i = 0; i < requiredBundleList.size(); i++) {
                @SuppressWarnings("unchecked")
                String bundle = (String) requiredBundleList.get(i);
                if (StringUtils.isBlank(bundle)) {
                    continue;
                }
                requiredBundlesSet.add(bundle);

            }
        }
        return excludedBundles;
    }

    /**
     * @return
     */
    public static boolean isReplaceXmls() {

        return StringUtils.isNotBlank(miniReplaceXmlConfig);
    }

    /**
     * @param replaceStrs
     */
    public static String[] loadReplaceXmls() {

        String[] replaceStrs = new String[]{};

        if (StringUtils.isNotBlank(miniReplaceXmlConfig)) {

            // Get mini-replace-xml flag
            List<?> miniReplaceXmlflaglist = CollectionConvertUtil
                    .arrayConvertToList(miniReplaceXmlConfig.split(","));

            // ��properties�ļ�
            Properties miniReplaceXmlConfig = PropertyFileUtil
                    .readProperties("/config/miniConf/mini-xml-config.properties");

            // ѭ������properties
            for (Object object : miniReplaceXmlflaglist) {

                String flag = object.toString();
                String value = miniReplaceXmlConfig.getProperty(flag);

                if (StringUtils.isNotBlank(value)) {
                    String[] noNeedLoadXmls = value.split(",");
                    for (String noNeedLoadXml : noNeedLoadXmls) {
                        replaceStrs = (String[]) ArrayUtils.add(replaceStrs,
                                noNeedLoadXml + "!META-INF\\spring\\test-replace-empty.xml");
                    }
                }

            }
        }
        return replaceStrs;
    }

    public static String getTestedBeanBundle(List<File> MFfiles, String testedInterface) {

        String bundleName = "";
        for (File file : MFfiles) {

            //忽略test-bundle的依赖分析
            if (file.getPath().contains("test")) {
                continue;
            }

            String springPath = StringUtils.substringBeforeLast(file.getAbsolutePath(), "/")
                    + "/spring";

            List<File> xmlBeanFileList = MFfileUtil.getSpeciFile(springPath, "(.*)xml$");
            for (File xmlFile : xmlBeanFileList) {
                //解析xml的bean Id
                BeanIdAnalysisHandler myHandler = new BeanIdAnalysisHandler();
                try {
                    XMLParserUtil.parseXML(xmlFile, myHandler);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                List<String> beanList = myHandler.getBeanList();

                for (int i = 0; i < beanList.size(); i++) {
                    if (StringUtils.equalsIgnoreCase(testedInterface, beanList.get(i))) {
                        bundleName = MFfileUtil.getModuleName(file);
                        break;
                    }
                }
                //找到bean定义处，跳出循环
                if (StringUtils.isNotBlank(bundleName)) {
                    break;
                }
            }
            //找到bean定义处，跳出循环
            if (StringUtils.isNotBlank(bundleName)) {
                break;
            }

        }

        return bundleName;
    }

}
