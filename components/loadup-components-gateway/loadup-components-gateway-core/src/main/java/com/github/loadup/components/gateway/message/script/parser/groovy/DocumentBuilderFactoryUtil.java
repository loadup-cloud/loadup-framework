package com.github.loadup.components.gateway.message.script.parser.groovy;

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

import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>
 * DocumentBuilderFactoryUtil.java
 * </p>
 */
public class DocumentBuilderFactoryUtil {
    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilderFactoryUtil.class);

    public static String EGE = "http://xml.org/sax/features/external-general-entities";
    public static String EPE = "http://xml.org/sax/features/external-parameter-entities";
    public static String LED = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static DocumentBuilderFactory createXXESafetyDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                DocumentBuilderFactoryUtil.class.getClassLoader());
        try {
            dbf.setFeature(LED, false);
            dbf.setFeature(EGE, false);
            dbf.setFeature(EPE, false);
        } catch (ParserConfigurationException e) {
            //请自行填充异常捕获逻辑
            LogUtil.error(logger, e, "Failed to set xml document features.");
        }
        return dbf;
    }
}
