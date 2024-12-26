package com.github.loadup.components.gateway.message.script.parser.groovy;

import org.springframework.core.io.AbstractResource;

import java.io.*;
import java.util.Arrays;

/**
 * 内存资源，主要用来将编译后的解析脚本存于内存
 */
public class InMemoryResource extends AbstractResource {

    /**
     * 资源描述
     */
    private static final String DESCRIPTION = "InMemoryResource";

    /**
     * 脚本来源
     */
    private final byte[] source;

    /**
     *
     */
    public InMemoryResource(String sourceString) {
        this.source = sourceString.getBytes();
    }

    /**
     * @see org.springframework.core.io.Resource#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @see org.springframework.core.io.InputStreamSource#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(source);
    }

    /**
     * @see AbstractResource#hashCode()
     */
    public int hashCode() {
        return Arrays.hashCode(source);
    }

    /**
     * @see AbstractResource#equals(Object)
     */
    public boolean equals(Object res) {
        if (!(res instanceof InMemoryResource)) {
            return false;
        }

        return Arrays.equals(source, ((InMemoryResource) res).source);
    }

}
