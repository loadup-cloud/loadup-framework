package com.github.loadup.components.gateway.certification.spi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 */
//@XObject("OutServiceDescriptor")
public class OutServiceDescriptor {

    /**
     * 组件的名字
     */
    //@XNode("@name")
    private String name;

    //@XNodeSpring("@listener")
    private OuterService outerService;

    /**
     * toSring
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
     * Getter method for property <tt>outerService<tt>.
     */
    public OuterService getOuterService() {
        return outerService;
    }

    /**
     * Setter method for property <tt>outerService<tt>.
     */
    public void setOuterService(OuterService outerService) {
        this.outerService = outerService;
    }
}