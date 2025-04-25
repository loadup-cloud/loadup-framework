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

import com.github.loadup.components.testify.log.TestifyLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * 用于搜索xml中指定的第一个属性名称
 */
@Slf4j
public class XMLUtil {

    /**
     * 读取xml文件
     *
     * @param xmlFile
     * @return
     */
    public static NodeList loadXMLFile(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            return doc.getChildNodes();
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "读取xml文件出错", e);
        }
        return null;
    }

    /**
     * 读取xmlFile指定第一次出现的属性内容
     *
     * @param xmlFile
     * @param attribute
     * @return
     * @throws Exception
     */
    public static String getAttribute(NodeList nodeList, String attribute) {
        String value = null;
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                value = getNode(nodeList.item(i), attribute);
                if (value != null)
                    return value;
            }
        }
        return value;
    }

    /**
     * 递归读取节点
     *
     * @param node
     * @param attribute
     * @return
     */
    private static String getNode(Node node, String attribute) {
        String value = null;
        if (node != null) {
            NamedNodeMap namedNodeMap = node.getAttributes();
            NodeList childNodeList = node.getChildNodes();
            if (namedNodeMap != null) {
                for (int j = 0; j < namedNodeMap.getLength(); j++) {
                    Node node1 = namedNodeMap.item(j);
                    if (node1.getNodeValue() != null && node1.getNodeValue().equals(attribute)) {
                        return namedNodeMap.item(j + 1).getNodeValue();
                    }
                    value = getNode(node1, attribute);
                    if (value != null)
                        return value;
                }
            }
            if (childNodeList != null) {
                for (int i = 0; i < childNodeList.getLength(); i++) {
                    Node node1 = childNodeList.item(i);
                    value = getNode(node1, attribute);
                    if (value != null)
                        return value;
                }
            }
        }
        return value;
    }
}
