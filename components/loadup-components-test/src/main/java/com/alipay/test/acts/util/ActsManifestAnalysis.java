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

import com.alipay.test.acts.object.tree.tree;
import com.alipay.test.acts.object.tree.treeNode;
import org.apache.commons.lang3.StringUtils;


import java.io.File;
import java.util.*;

/**
 * 根据MANIFEST.MF解析依赖树工具类
 *
 * @author mokong
 * @version com.alipay.acctrans.test.util 2015-9-14
 */
public class ActsManifestAnalysis {

    static Map<String, tree<String>> treeMap = new HashMap<String, tree<String>>();

    public static void main(String[] args) {

        //String scriptPath = "/Users/xiaoleicxl/workspace-alibank/fcdepcore/branch/spi/app";

        String scriptPath = "/Users/xiaoleicxl/workspace-alibank/acctrans/acctrans";
        System.out.println(scriptPath);
        String scriptFileNameRegex = "pom.xml";

        HashSet<String> fullBundle = getFullBundle(scriptPath, scriptFileNameRegex);
        System.out.println("---------全量bundle----------");
        System.out.println(fullBundle);

        System.out.println("---------根bundle----------");
        System.out.println(treeMap.keySet());

        System.out.println("---------依赖的bundle----------");
        HashSet<String> bundleNeedtoLoad = getRequiredBundles("com.alipay.acctrans.core.config");
        System.out.println(bundleNeedtoLoad);

        System.out.println("---------剔除的bundle----------");
        HashSet<String> bundleNoNeedtoLoad = getExcludeBundles("com.alipay.acctrans.core.config");
        System.out.println(bundleNoNeedtoLoad);

        //        System.out.println("---------剔除的bundle----------");
        //        HashSet<String> bundleNoNeedtoLoad2 = getExcludeBundlesByRequiredBundleSet("com.alipay.fc.financecore.biz.service.impl");
        //        System.out.println(bundleNoNeedtoLoad2);

    }

    /**
     * 根据系统根目录查找全部MANIFEST.MF，并依次解析依赖关系
     *
     * @param scriptPath
     * @param scriptFileNameRegex
     */
    private static void genManifestInfo(String scriptPath, String scriptFileNameRegex) {

        List<File> scriptFileList = new ArrayList<File>();

        //改成拿pom文件
        FileOperateUtils.findFileRecursive(scriptPath, scriptFileNameRegex, scriptFileList);
        for (File script : scriptFileList) {
            //忽略test-bundle的依赖分析
            if (script.getPath().contains("test")) {
                continue;
            }
            analysisManifestInfo(script);
        }

    }

    /**
     * 根据MANIFEST.MF，解析bundle依赖关系
     *
     * @param script
     */
    private static void analysisManifestInfo(File bundlePom) {

        try {

            PomDependencyAnalysisHandler myHandler = new PomDependencyAnalysisHandler();

            XMLParserUtil.parseXML(bundlePom, myHandler);

            //过滤非bundle的pom文件
            if (!myHandler.isBundlePom()) {
                return;
            }

            String currentBundle = myHandler.getCurrentBundle();
            List<String> requiredBundle = myHandler.getDependencyBundleList();

            //增加该bundle根节点
            tree<String> bundleTree = new tree<String>();
            bundleTree.addNode(null, currentBundle);
            //增加依赖子节点
            for (String s : requiredBundle) {
                bundleTree.addNode(bundleTree.getNode(currentBundle), s);
            }

            if (!treeMap.keySet().contains(currentBundle)) {

                treeMap.put(currentBundle, bundleTree);

                //System.out.println("******");
                bundleTree.showNode(bundleTree.root);
                //System.out.println(currentBundle);
                //System.out.println(requiredBundle);

            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }

    /**
     * 获取全量bundle
     *
     * @param scriptPath
     * @param scriptFileNameRegex
     * @return
     */
    public static HashSet<String> getFullBundle(String scriptPath, String scriptFileNameRegex) {
        HashSet<String> fullBundleSet = new HashSet<String>();
        genManifestInfo(scriptPath, scriptFileNameRegex);
        for (String s : treeMap.keySet()) {

            fullBundleSet.addAll(getRequiredBundles(treeMap.get(s).root.t));
        }
        return fullBundleSet;
    }

    /**
     * 获取依赖的bundle
     *
     * @param rootBundleName
     * @return
     */
    private static HashSet<String> getRequiredBundles(String rootBundleName) {

        String xmlRootBundleName = "";

        //MF的bundle转换成xml中模糊匹配的bundle名称
        List<String> mfBundleStringList = Arrays.asList(StringUtils.split(rootBundleName, "."));
        for (String s : treeMap.keySet()) {
            String[] xmlBundleStringArray = StringUtils.split(s, ".");
            List<String> xmlBundleStringList = Arrays.asList(xmlBundleStringArray);
            if (xmlBundleStringList.containsAll(mfBundleStringList)) {
                xmlRootBundleName = s;
                break;
            }
        }

        tree<String> tree = treeMap.get(xmlRootBundleName);
        HashSet<String> bundleNeedtoLoad = new HashSet<String>();
        findRefNode(tree.getNode(xmlRootBundleName), bundleNeedtoLoad);
        return bundleNeedtoLoad;
    }

    /**
     * 获取指定bundle依赖树的子结点
     *
     * @param node
     * @param bundleNeedtoLoad
     */
    public static void findRefNode(treeNode<String> node, HashSet<String> bundleNeedtoLoad) {
        if (null != node) {

            //循环遍历依赖的node的节点

            if (!bundleNeedtoLoad.contains(node.t.toString())) {
                bundleNeedtoLoad.add(node.t.toString());
            }
            for (int i = 0; i < node.nodelist.size(); i++) {
                tree<String> tree = treeMap.get(node.nodelist.get(i).t);
                if (tree != null) {
                    findRefNode(tree.getNode(node.nodelist.get(i).t), bundleNeedtoLoad);
                } else {
                    if (!bundleNeedtoLoad.contains(node.nodelist.get(i).t)) {
                        bundleNeedtoLoad.add(node.nodelist.get(i).t);
                    }
                }
            }
        }
    }

    /**
     * 获取指定bundle不依赖的bundle
     *
     * @param rootBundleName
     * @return
     */
    public static HashSet<String> getExcludeBundles(String rootBundleName) {
        ActsManifestAnalysis.getFullBundle(getAppPath(), "pom.xml");//先初始化全量bundle
        HashSet<String> bundleNeedtoLoad = getRequiredBundles(rootBundleName);
        HashSet<String> bundleNoNeedtoLoad = new HashSet<String>();
        for (String s : treeMap.keySet()) {
            if (!bundleNeedtoLoad.contains(s)) {
                bundleNoNeedtoLoad.add(s);
            }
        }
        return bundleNoNeedtoLoad;

    }

    /**
     * 根据依赖bundle集合获取不依赖的Bundle集合
     *
     * @param rootBundleName
     * @return
     */
    public static HashSet<String> getExcludeBundlesByRequiredBundleSet(HashSet<String> requiredBunldeSet) {
        HashSet<String> excludedBundleSet = new HashSet<String>();

        HashSet<String> fullBundlesSet = ActsManifestAnalysis
                .getFullBundle(getAppPath(), "pom.xml");
        //获取排除的bundle
        for (String s : fullBundlesSet) {
            if (!requiredBunldeSet.contains(s)) {
                excludedBundleSet.add(s);
            }
        }
        return excludedBundleSet;

    }

    /**
     * 获取app的根目录
     *
     * @return
     */
    public static String getAppPath() {
        String testBundlePath = System.getProperty("user.dir");
        String sepratorString = "app";
        String appPath = "";

        if (!StringUtils.equals(StringUtils.substringAfterLast(testBundlePath, sepratorString), "")) {
            //sofa4用app分割
            appPath = StringUtils.substringBeforeLast(testBundlePath, sepratorString);

        } else {
            //sofa3用系统名分割
            sepratorString = "";// TestUtils.getSofaConfig("app_name");//第一个元素是系统名，取第二个元素如biz等作为分割
            appPath = StringUtils.substringBeforeLast(testBundlePath, sepratorString);

        }

        return appPath;
    }

}
