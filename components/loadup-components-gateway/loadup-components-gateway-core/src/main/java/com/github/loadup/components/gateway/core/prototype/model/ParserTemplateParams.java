package com.github.loadup.components.gateway.core.prototype.model;

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

    /**
     *
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     *
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     *
     */
    public Map<String, String> getTemplateParams() {
        return templateParams;
    }

    /**
     *
     */
    public void setTemplateParams(Map<String, String> templateParams) {
        this.templateParams = templateParams;
    }
}
