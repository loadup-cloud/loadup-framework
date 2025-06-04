/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * CE专用Groovy兼容处理类
 */
public class DelegatedEntityResolver implements EntityResolver {

    private final Map<EntityResolver, String> resolvers = new LinkedHashMap<EntityResolver, String>(2);

    /**
     * CE兼容性处理
     */
    public void addEntityResolver(EntityResolver resolver, String resolverToString) {
        resolvers.put(resolver, resolverToString);
    }

    /**
     * @see EntityResolver#resolveEntity(String, String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        for (Iterator<Entry<EntityResolver, String>> iterator =
                        resolvers.entrySet().iterator();
                iterator.hasNext(); ) {
            Entry<EntityResolver, String> entry = iterator.next();
            EntityResolver entityResolver = entry.getKey();

            InputSource entity = entityResolver.resolveEntity(publicId, systemId);

            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
