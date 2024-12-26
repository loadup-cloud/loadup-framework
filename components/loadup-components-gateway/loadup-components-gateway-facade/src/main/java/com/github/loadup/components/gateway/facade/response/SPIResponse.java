package com.github.loadup.components.gateway.facade.response;

import com.github.loadup.components.gateway.facade.model.Result;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 *
 */
public class SPIResponse {

    /**
     * 响应报文
     */
    private String content;

    /**
     * headers
     */
    private Map<String, String> headers;

    /**
     * gateway process result
     */
    private Result result;

    public SPIResponse() {
    }

    public SPIResponse(String content, Map<String, String> headers, Result result) {
        this.content = content;
        this.headers = headers;
        this.result = result;
    }

    /**
     * Getter method for property <tt>content</tt>.
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter method for property <tt>content</tt>.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter method for property <tt>headers</tt>.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Setter method for property <tt>headers</tt>.
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Getter method for property <tt>result</tt>.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Setter method for property <tt>result</tt>.
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}