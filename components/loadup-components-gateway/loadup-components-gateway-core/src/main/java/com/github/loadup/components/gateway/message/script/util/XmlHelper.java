package com.github.loadup.components.gateway.message.script.util;

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

import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XML 操作助手，使用Dom4j完成dom相关操作
 */
public final class XmlHelper {

    /**
     * logger
     */
    protected static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    /**
     * 禁用构造函数
     */
    private XmlHelper() {
        // 禁用构造函数
    }

    /**
     * 报文原型转换成MAP对象，保持原来的顺序<br><br>
     * <p>
     * 报文原型举例：
     *
     * <pre>
     * &lt;xml>
     *     &lt;map name=”merchantNo”>123456&lt;/map>
     *     &lt;map name=”merchantName”>仲景&lt;/map>
     * &lt;/xml>
     * </pre>
     * <p>
     * 就返回键值对的Map<String, String>数据结构方式：
     *
     * <ul>
     *   <li>merchantNo - 123456</li>
     *   <li>merchantName - 仲景</li>
     * </ul>
     *
     * @throws DocumentException
     */
    public static Map<String, String> convertPrototype(String messagePrototype)
            throws DocumentException {
        List<Element> fields = getFields(messagePrototype, "map");

        Map<String, String> paramMap = new LinkedHashMap<String, String>();
        for (Element field : fields) {
            String fieldName = field.attributeValue("name");
            String fieldValue = field.getText();
            paramMap.put(fieldName, fieldValue);
        }

        return paramMap;
    }

    /**
     * 解析XML字符串，获取指定的每个节点
     *
     * @throws DocumentException
     */
    public static List<Element> getFields(String xml, String node) throws DocumentException {
        Element root = getField(xml);
        return children(root, node);
    }

    /**
     * 解析XML字符串，获取指定的每个节点
     *
     * @throws DocumentException
     */
    public static List<Element> getFields(String xml, String node, String subNode)
            throws DocumentException {
        Element root = getField(xml);
        Element nodeElement = child(root, node);
        return children(nodeElement, subNode);
    }

    /**
     * 获取XML报文的根节点
     *
     * @throws DocumentException
     */
    public static Element getField(String xml) throws DocumentException {
        StringReader stringReader = null;

        try {
            stringReader = new StringReader(xml);
            SAXReader reader = new SAXReader();
            try {
                reader.setFeature(Constant.DDD, true);
            } catch (SAXException e) {
                LogUtil.debug(logger, "Prevention XXE injection setting failed。");
            }
            Document doc = reader.read(stringReader);
            return doc.getRootElement();
        } finally {
            IOUtils.closeQuietly(stringReader);
        }

    }

    /**
     * Return the child element with the given name. The element must be in the
     * same name space as the parent element.
     */
    public static Element child(Element element, String name) {
        return element.element(new QName(name, element.getNamespace()));
    }

    /**
     * Return the child elements with the given name. The elements must be in
     * the same name space as the parent element.
     */
    @SuppressWarnings("unchecked")
    public static List<Element> children(Element element, String name) {
        return element.elements(new QName(name, element.getNamespace()));
    }

    /**
     * Return the value of the child element with the given name. The element
     * must be in the same name space as the parent element.
     */
    public static String elementAsString(Element element, String name) {
        return element.elementTextTrim(new QName(name, element.getNamespace()));
    }

    /**
     * 转化元素为int
     */
    public static int elementAsInteger(Element element, String name) {
        String text = elementAsString(element, name);
        if (text == null) {
            return 0;
        }

        return Integer.parseInt(text);
    }

    /**
     * 转化元素为boolean
     */
    public static boolean elementAsBoolean(Element element, String name) {
        String text = elementAsString(element, name);
        if (text == null) {
            return false;
        }

        return Boolean.valueOf(text);
    }

}
