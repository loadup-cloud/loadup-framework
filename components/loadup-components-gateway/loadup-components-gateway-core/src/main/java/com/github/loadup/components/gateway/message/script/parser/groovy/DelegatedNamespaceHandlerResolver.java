package com.github.loadup.components.gateway.message.script.parser.groovy;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;

import java.util.*;
import java.util.Map.Entry;

/**
 * CE专用Groovy兼容处理类
 */
public class DelegatedNamespaceHandlerResolver implements NamespaceHandlerResolver {

    private final Map<NamespaceHandlerResolver, String> resolvers = new LinkedHashMap<NamespaceHandlerResolver, String>(
            2);

    /**
     * CE兼容性处理
     */
    public void addNamespaceHandler(NamespaceHandlerResolver resolver, String resolverToString) {
        resolvers.put(resolver, resolverToString);
    }

    /**
     * @see NamespaceHandlerResolver#resolve(String)
     */
    @Override
    public NamespaceHandler resolve(String namespaceUri) {

        for (Iterator<Entry<NamespaceHandlerResolver, String>> iterator = resolvers.entrySet()
                .iterator(); iterator.hasNext(); ) {
            Entry<NamespaceHandlerResolver, String> entry = iterator.next();
            NamespaceHandlerResolver handlerResolver = entry.getKey();
            NamespaceHandler handler = handlerResolver.resolve(namespaceUri);

            if (handler != null) {
                return handler;
            }

        }
        return null;
    }
}