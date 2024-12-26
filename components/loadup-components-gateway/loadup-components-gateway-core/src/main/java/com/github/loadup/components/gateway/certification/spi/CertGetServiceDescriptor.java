package com.github.loadup.components.gateway.certification.spi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 证书获取服务描述
 */
//@XObject("CertGetServiceDescriptor")
public class CertGetServiceDescriptor {

    /**
     * 组件的名字
     */
    //@XNode("@name")
    private String name;

    //@XNodeSpring("@listener")
    private CertGetService certGetService;

    /**
     * toString
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);

    }

    /**
     * Getter method for property <tt>name<tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name<tt>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>certGetService<tt>.
     */
    public CertGetService getCertGetService() {
        return certGetService;
    }

    /**
     * Setter method for property <tt>certGetService<tt>.
     */
    public void setCertGetService(CertGetService certGetService) {
        this.certGetService = certGetService;
    }
}