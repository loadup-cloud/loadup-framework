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

import org.springframework.core.io.AbstractResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
