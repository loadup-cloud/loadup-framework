package com.github.loadup.components.gateway.core.model.common;

import com.github.loadup.components.gateway.core.common.enums.MessageFormat;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * 报文信息载体，可以简单理解为“信封”<br>
 * 组装之后的请求报文和等待解析的响应报文，可以形象理解为保存在信封中，邮寄来邮寄去，<br>
 * <p>
 * 于是，通讯组件的数据请求和响应都是基于这个信息载体
 */
public final class MessageEnvelope implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 9158087747318597902L;

    /**
     * 报文正文格式
     */
    private MessageFormat messageFormat;

    /**
     * 报文正文信息
     */
    private Object content;

    /**
     * 报文日志描述，可以屏蔽敏感信息
     */
    private String logContent;

    /**
     * 报文需要签名
     */
    private boolean needVerifySignature = false;

    /**
     * 报文附加信息，例如HTTP报文头的附加信息
     */
    private Map<String, String> headers;

    /**
     * 额外的数据信息
     */
    private Map<String, String> extraMap;

    /**
     * 签名字段
     */
    private String signatureContent;

    /**
     * 原始交易字段
     */
    private String originSignContent;

    /**
     * 签名需要用到的code
     */
    private String signatureCertCode;

    /**
     * 构造函数
     */
    public MessageEnvelope(MessageFormat messageFormat, Object content) {
        this.messageFormat = messageFormat;
        this.content = content;
        this.headers = new HashMap<String, String>();
    }

    /**
     * 构造函数
     */
    public MessageEnvelope(MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
        this.headers = new HashMap<String, String>();
    }

    /**
     *
     */
    public void putExtMap(String key, String value) {
        if (null == this.extraMap) {
            this.extraMap = new HashMap<String, String>();
        }
        this.extraMap.put(key, value);
    }

    /**
     * return extra value
     */
    public String pullExtMapValue(String key) {
        if (null == this.extraMap) {
            return StringUtils.EMPTY;
        }
        return this.extraMap.get(key);
    }

    /**
     * Gets get signature content.
     */
    public String getSignatureContent() {
        return signatureContent;
    }

    /**
     * Sets set signature content.
     */
    public void setSignatureContent(String signatureContent) {
        this.signatureContent = signatureContent;
    }

    /**
     * Gets get origin content.
     */
    public String getOriginSignContent() {
        return originSignContent;
    }

    /**
     * Sets set origin content.
     */
    public void setOriginSignContent(String originSignContent) {
        this.originSignContent = originSignContent;
    }

    /**
     * Is need verify signature boolean.
     */
    public boolean isNeedVerifySignature() {
        return needVerifySignature;
    }

    /**
     * Sets set need verify signature.
     */
    public void setNeedVerifySignature(boolean needVerifySignature) {
        this.needVerifySignature = needVerifySignature;
    }

    /**
     * 构造函数
     */
    public MessageEnvelope(MessageFormat format, Object content, Map<String, String> headers) {
        this.messageFormat = format;
        this.content = content;
        if (headers != null) {
            this.headers = headers;
        } else {
            this.headers = new HashMap<String, String>();
        }
    }

    /**
     * 检查信息是否为空
     */
    @SuppressWarnings("rawtypes")
    public boolean isEmpty() {
        if (this.content == null) {
            return true;
        }

        boolean empty = true;
        switch (messageFormat) {
            case TEXT: {
                empty = ((String) this.content).isEmpty();
                break;
            }
            case MULTI_PART:
            case MAP: {
                empty = ((Map) this.content).isEmpty();
                break;
            }
            case BYTE: {
                empty = ((byte[]) this.content).length == 0;
                break;
            }
            default: {
                return empty;
            }
        }

        return empty;
    }

    /**
     * Clone message envelope.
     */
    @Override
    public MessageEnvelope clone() {
        MessageEnvelope messageEnvelope = new MessageEnvelope(messageFormat, content);
        messageEnvelope.setLogContent(logContent);
        messageEnvelope.setHeaders(this.headers);
        return messageEnvelope;
    }

    /**
     * Getter method for property <tt>messageFormat</tt>.
     */
    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

    /**
     * Setter method for property <tt>content</tt>.
     */
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * Getter method for property <tt>extraContent</tt>.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Setter method for property <tt>extraContent</tt>.
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Getter method for property <tt>logContent</tt>.
     */
    public String getLogContent() {
        return logContent;
    }

    /**
     * Setter method for property <tt>logContent</tt>.
     */
    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {

        return " MessageEnvelope[" + "messageFormat=" + this.messageFormat + ',' +
                ",extraContent=" + this.headers +
                ']';
    }

    /**
     * Getter method for property <tt>content</tt>.
     */
    public Object getContent() {
        return content;
    }

    /**
     * return the string content
     */
    public String getStrContent() {
        if (Objects.isNull(this.content)) {
            return null;
        }
        return String.valueOf(this.content);
    }

    /**
     * Gets get signature cert code.
     */
    public String getSignatureCertCode() {
        return signatureCertCode;
    }

    /**
     * Sets set signature cert code.
     */
    public void setSignatureCertCode(String signatureCertCode) {
        this.signatureCertCode = signatureCertCode;
    }
}
