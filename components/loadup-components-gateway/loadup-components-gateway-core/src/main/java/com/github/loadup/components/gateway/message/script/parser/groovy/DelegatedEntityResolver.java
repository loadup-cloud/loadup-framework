package com.github.loadup.components.gateway.message.script.parser.groovy;

import org.xml.sax.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * CE专用Groovy兼容处理类
 */
public class DelegatedEntityResolver implements EntityResolver {

    private final Map<EntityResolver, String> resolvers = new LinkedHashMap<EntityResolver, String>(
            2);

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
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
            IOException {

        for (Iterator<Entry<EntityResolver, String>> iterator = resolvers.entrySet().iterator(); iterator
                .hasNext(); ) {
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
