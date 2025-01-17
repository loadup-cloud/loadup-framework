package com.github.loadup.components.gateway.certification.util;

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

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.utils.URI;
import org.w3c.dom.Attr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class helps us home users to resolve http URIs without a network
 * connection
 */
public class OfflineResolver extends ResourceResolverSpi {
    /**
     * {@link org.apache.commons.logging} logging facility
     */
    static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(OfflineResolver.class.getName());

    /**
     * Field _uriMap
     */
    static Map<String, String> uriMap = null;

    /**
     * Field _mimeMap
     */
    static Map<String, String> mimeMap = null;

    static {
        org.apache.xml.security.Init.init();

        uriMap = new HashMap<String, String>();
        mimeMap = new HashMap<String, String>();

        OfflineResolver.register("http://www.w3.org/TR/xml-stylesheet",
                "data/org/w3c/www/TR/xml-stylesheet.html", "text/html");
        OfflineResolver.register("http://www.w3.org/TR/2000/REC-xml-20001006",
                "data/org/w3c/www/TR/2000/REC-xml-20001006", "text/xml");
        OfflineResolver.register("http://www.nue.et-inf.uni-siegen.de/index.html",
                "data/org/apache/xml/security/temp/nuehomepage", "text/html");
        OfflineResolver.register("http://www.nue.et-inf.uni-siegen.de/~geuer-pollmann/id2.xml",
                "data/org/apache/xml/security/temp/id2.xml", "text/xml");
        OfflineResolver.register("http://xmldsig.pothole.com/xml-stylesheet.txt",
                "data/com/pothole/xmldsig/xml-stylesheet.txt", "text/xml");
        OfflineResolver.register("http://www.w3.org/Signature/2002/04/xml-stylesheet.b64",
                "data/ie/baltimore/merlin-examples/merlin-xmldsig-twenty-three/xml-stylesheet.b64",
                "text/plain");
    }

    /**
     * Method register
     */
    private static void register(String URI, String filename, String MIME) {
        uriMap.put(URI, filename);
        mimeMap.put(URI, MIME);
    }

    /**
     * Method engineResolve
     *
     * @throws ResourceResolverException
     */
    @Override
    public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
            throws ResourceResolverException {

        try {
            String URI = uri.getNodeValue();

            if (OfflineResolver.uriMap.containsKey(URI)) {
                String newURI = OfflineResolver.uriMap.get(URI);
                if (log.isDebugEnabled()) {
                    log.debug("Mapped " + URI + " to " + newURI);
                }

                try (InputStream is = new FileInputStream(newURI)) {

                    if (log.isDebugEnabled()) {
                        log.debug("Available bytes = " + is.available());
                    }

                    XMLSignatureInput result = new XMLSignatureInput(is);

                    // XMLSignatureInput result = new XMLSignatureInput(inputStream);
                    result.setSourceURI(URI);
                    result.setMIMEType(OfflineResolver.mimeMap.get(URI));

                    return result;
                }
            } else {
                Object exArgs[] = {"The URI " + URI + " is not configured for offline work"};

                throw new ResourceResolverException("generic.EmptyMessage", exArgs, uri, BaseURI);
            }
        } catch (IOException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, uri, BaseURI);
        }
    }

    /**
     * We resolve http URIs <I>without</I> fragment...
     */
    @Override
    public boolean engineCanResolve(Attr uri, String BaseURI) {

        String uriNodeValue = uri.getNodeValue();

        if ("".equals(uriNodeValue) || uriNodeValue.startsWith("#")) {
            return false;
        }

        try {
            URI uriNew = new URI(new URI(BaseURI), uri.getNodeValue());

            if ("http".equals(uriNew.getScheme())) {
                if (log.isDebugEnabled()) {
                    log.debug("I state that I can resolve " + uriNew.toString());
                }

                return true;
            }
            if (log.isDebugEnabled()) {
                log.debug("I state that I can't resolve " + uriNew.toString());
            }
        } catch (URI.MalformedURIException ex) {
            log.error("URI地址解析失败！", ex);

        }

        return false;
    }

}
