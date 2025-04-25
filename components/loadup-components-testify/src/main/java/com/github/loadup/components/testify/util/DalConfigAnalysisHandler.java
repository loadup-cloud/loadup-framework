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
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.lang3.StringUtils;

/**
 * dal-config.xml解析处理器
 *
 * 
 *
 */
class DalConfigAnalysisHandler extends DefaultHandler {
    // 使用栈这个数据结构来保存
    private Stack<String> stack = new Stack<String>();

    private List<String> findIncludeList = new ArrayList<String>();

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

        if (StringUtils.equals(qName, "include")) {

            findIncludeList.add(attributes.getValue(0));
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

    }

    /**
     * 根据解析结果获取表名set
     *
     * @return
     */
    public HashSet<String> getFullTables() {
        HashSet<String> tableSet = new HashSet<String>();
        for (String s : findIncludeList) {
            String tableTemp = StringUtils.substringAfterLast(s, "tables/");
            String table = StringUtils.substringBefore(tableTemp, ".");
            tableSet.add(table);
        }

        return tableSet;
    }
}
