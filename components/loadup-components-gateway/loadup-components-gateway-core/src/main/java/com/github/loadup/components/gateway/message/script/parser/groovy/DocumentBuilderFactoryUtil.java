package com.github.loadup.components.gateway.message.script.parser.groovy;

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