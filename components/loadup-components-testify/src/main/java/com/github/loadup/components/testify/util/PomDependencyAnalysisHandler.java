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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.lang3.StringUtils;

/**
 * pom.xml  依赖关系解析
 *
 * 
 *
 */
class PomDependencyAnalysisHandler extends DefaultHandler {
    // 使用栈这个数据结构来保存
    private Stack<String> stack = new Stack<String>();
    private boolean findArtifactId = false;
    private boolean findGroupId = false;
    private boolean findParent = false;
    private boolean findDependency = false;
    private boolean findCurrentBundleArtifactId = false;
    private boolean findCurrentBundleGroupId = false;
    private boolean isBundlePom = true;
    private List<String> findGroupIdList = new ArrayList<String>();
    private List<String> findArtifactIdList = new ArrayList<String>();
    private String currentBundleArtifactId = null;
    private String currentBundleGroupId = null;

    @Override
    public void startDocument() throws SAXException {
        //System.out.println("start document -> parse begin");
    }

    @Override
    public void endDocument() throws SAXException {

        //System.out.println("end document -> parse finished");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        //        System.out.println("start element-----------");
        //        System.out.println("    localName: " + localName);
        //        System.out.println("    qName: " + qName);

        //过滤非bundle的pom
        if (StringUtils.equals(qName, "modules")) {
            isBundlePom = false;
        }

        if (StringUtils.equals(qName, "dependency")) {
            findDependency = true;
        }

        if (StringUtils.equals(qName, "parent")) {
            findParent = true;
        }

        //找到依赖bundle的groupId和artifactId

        if (findDependency && StringUtils.equals(qName, "groupId")) {
            findGroupId = true;
        }

        if (findDependency && StringUtils.equals(qName, "artifactId")) {
            findArtifactId = true;
        }

        //找到当前bundle的groupId和artifactId
        if (!findParent && !findDependency && StringUtils.equals(qName, "groupId")) {
            findCurrentBundleGroupId = true;
        }
        if (!findParent && !findDependency && StringUtils.equals(qName, "artifactId")) {
            findCurrentBundleArtifactId = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        //        System.out.println("characters-----------");
        //        // System.out.println("    ch: " + Arrays.toString(ch) );
        //        System.out.println("    ch: " + ch);
        //        System.out.println("    start: " + start);
        //        System.out.println("    length: " + length);
        if (findGroupId) {
            String s = new String(ch, start, length);
            findGroupIdList.add(s);
            findGroupId = false;
        }

        if (findArtifactId) {
            String s = new String(ch, start, length);
            findArtifactIdList.add(s);
            findArtifactId = false;
        }

        if (findCurrentBundleGroupId) {
            String s = new String(ch, start, length);
            //当前bundle的artifactId放在第二个位置
            //findArtifactIdList.add(1, s);
            currentBundleGroupId = s;
            findCurrentBundleGroupId = false;
        }

        if (findCurrentBundleArtifactId) {
            String s = new String(ch, start, length);
            //当前bundle的artifactId放在第二个位置
            //findArtifactIdList.add(1, s);
            currentBundleArtifactId = s;
            findCurrentBundleArtifactId = false;
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //        System.out.println("end element-----------");
        //        System.out.println("    localName: " + localName);
        //        System.out.println("    qName: " + qName);

        if (StringUtils.equals(qName, "dependency")) {
            findDependency = false;
        }

        if (StringUtils.equals(qName, "parent")) {
            findParent = false;
        }
    }

    /**
     * 获取当前bundle的依赖bundle
     *
     * @return
     */
    public List<String> getDependencyBundleList() {

        List<String> dependencyProjectBundleList = new ArrayList<String>();
        //过滤
        String filtString = getFiltString();
        for (int i = 0; i < findArtifactIdList.size(); i++) {
            if (findArtifactIdList.get(i).contains(filtString)) {

                //拼接依赖bundle name
                dependencyProjectBundleList.add(getFuzzyBundleName(findGroupIdList.get(i),
                        findArtifactIdList.get(i)));
            }
        }
        return dependencyProjectBundleList;
    }

    /**
     * 获取当前bundle
     *
     * @return
     */
    public String getCurrentBundle() {

        return getFuzzyBundleName(currentBundleGroupId, currentBundleArtifactId);
    }

    /**
     * 获取bundle过滤字符串
     *
     * @return
     */
    public String getFiltString() {
        //获取当前pom的artifactId如“fcfinancecore-common-service-facade”，按“－”分割后取第一个字符串作为过滤字串
        String filtString = (currentBundleArtifactId.split("-"))[0];
        return filtString;
    }

    /**
     * Bundle名称拼接，有些系统命名不标准，此中方法匹配pom.xml和MANIFEST.MF中bundle name的映射关系的成功率低
     *
     * @return
     */
    public String getBundleName(String groupId, String artifactId) {
        //获得分割字串
        String bundleName = null;
        String[] groupIdList = StringUtils.split(groupId, ".");
        String spiltString = groupIdList[groupIdList.length - 1];

        //用分割字串分割artifactId
        String artifactIdTemp = StringUtils.substringAfterLast(artifactId, spiltString);

        String[] artifactIdList = artifactIdTemp.split("-");

        //拼接bundleName
        bundleName = groupId;
        for (String s : artifactIdList) {
            if (!s.isEmpty()) {
                bundleName = bundleName + "." + s;
            }

        }
        return bundleName;
    }


    /**
     * 模糊Bundle名称拼接，直接将groupId和artifactId的字段拼接
     *
     * @return
     */
    public String getFuzzyBundleName(String groupId, String artifactId) {
        //获得分割字串
        String bundleName = "";

        //用分割字串分割groupId
        String[] groupIdList = StringUtils.split(groupId, ".");

        //用分割字串分割artifactId
        String[] artifactIdList = artifactId.split("-");

        //拼接bundleName
        for (String s : groupIdList) {
            if (!s.isEmpty()) {
                bundleName = bundleName + "." + s;
            }

        }

        for (String s : artifactIdList) {
            if (!s.isEmpty()) {
                bundleName = bundleName + "." + s;
            }

        }
        return bundleName;
    }

    /**
     * Bundle名称拼接
     *
     * @return
     */
    public boolean isBundlePom() {
        return isBundlePom;
    }

    /**
     * 
     *
     * @return property value of findArtifactIdList
     */
    public List<String> getfindArtifactIdList() {
        return findArtifactIdList;
    }
}
