package com.github.loadup.components.gateway.core.prototype.model;

import com.github.loadup.components.gateway.core.common.Constant;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 解析模板参数
 */
public class ParserTemplateParams {

    private String templateName;

    private Map<String, String> templateParams;

    /**
     * Getter method for property <tt>templateName</tt>.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Setter method for property <tt>templateName</tt>.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Getter method for property <tt>templateParams</tt>.
     */
    public Map<String, String> getTemplateParams() {
        return templateParams;
    }

    /**
     * Setter method for property <tt>templateParams</tt>.
     */
    public void setTemplateParams(Map<String, String> templateParams) {
        this.templateParams = templateParams;
    }

    /**
     *
     */
    public static ParserTemplateParams buildByConfigString(String configString) {
        int index = configString.indexOf(Constant.VALUE_SEPARATOR_COLON);
        String templateName = configString.substring(0, index);
        String paramsString = configString.substring(index + 1);
        Map<String, String> templateParams = new HashMap<>();
        Stream.of(paramsString.split(Constant.COMMA_SEPARATOR)).forEach(m -> {
            String[] split = m.split(Constant.VALUE_SEPARATOR);
            templateParams.put(split[0], split[1]);
        });
        ParserTemplateParams parserTemplateParams = new ParserTemplateParams();
        parserTemplateParams.setTemplateName(templateName);
        parserTemplateParams.setTemplateParams(templateParams);
        return parserTemplateParams;
    }
}