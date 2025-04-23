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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.lang3.StringUtils;


/**
 * <bean id="lcDailyCutNoticeService"
        class="com.alipay.fc.loancore.biz.service.impl.api.loantrade.LcDailyCutNoticeServiceImpl" />

 * 解析全量bean Id
 * @author xiaoleicxl
 * @version $Id: MockBeanAnalysisHandler.java, v 0.1 2016年1月4日 上午10:44:03 xiaoleicxl Exp $
 */
public class BeanIdAnalysisHandler extends DefaultHandler {

    private List<String> beanList = new ArrayList<String>();

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

        if (StringUtils.equals(qName, "bean")) {

            beanList.add(attributes.getValue(0));

        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

    }

    /**
     * @return the beanList
     */
    public List<String> getBeanList() {
        return beanList;
    }


}
