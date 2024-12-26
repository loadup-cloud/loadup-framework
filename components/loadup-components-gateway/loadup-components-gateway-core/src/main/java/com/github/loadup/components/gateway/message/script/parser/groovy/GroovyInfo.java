package com.github.loadup.components.gateway.message.script.parser.groovy;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * GROOVY脚本信息
 */
public class GroovyInfo {

    /**
     * 类名
     */
    private String className;

    /**
     * 脚本内容
     */
    private String groovyContent;

    /**
     * Getter method for property <tt>className</tt>.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Setter method for property <tt>className</tt>.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter method for property <tt>groovyContent</tt>.
     */
    public String getGroovyContent() {
        return groovyContent;
    }

    /**
     * Setter method for property <tt>groovyContent</tt>.
     */
    public void setGroovyContent(String groovyContent) {
        this.groovyContent = groovyContent;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((className == null) ? 0 : className.hashCode());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        GroovyInfo other = (GroovyInfo) obj;
        return StringUtils.equals(other.className, this.className);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "groovyInfo[className=" + className + "]";
    }

}
