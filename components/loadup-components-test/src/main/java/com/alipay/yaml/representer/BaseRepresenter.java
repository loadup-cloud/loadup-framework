/**
 * Copyright (c) 2008-2013, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.yaml.representer;

/*-
 * #%L
 * loadup-components-test
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

import com.alipay.yaml.DumperOptions.FlowStyle;
import com.alipay.yaml.DumperOptions.ScalarStyle;
import com.alipay.yaml.introspector.PropertyUtils;
import com.alipay.yaml.nodes.*;

import java.util.*;

/**
 * Represent basic YAML structures: scalar, sequence, mapping
 */
public abstract class BaseRepresenter {
    protected final Map<Class<?>, Represent> representers          = new HashMap<Class<?>, Represent>();
    /**
     * in Java 'null' is not a type. So we have to keep the null representer
     * separately otherwise it will coincide with the default representer which
     * is stored with the key null.
     */
    protected Represent                      nullRepresenter;
    // the order is important (map can be also a sequence of key-values)
    protected final Map<Class<?>, Represent> multiRepresenters     = new LinkedHashMap<Class<?>, Represent>();
    protected Character                      defaultScalarStyle;
    protected FlowStyle                      defaultFlowStyle      = FlowStyle.AUTO;
    protected final Map<Object, Node>        representedObjects    = new IdentityHashMap<Object, Node>() {
                                                                       private static final long serialVersionUID = -5576159264232131854L;

                                                                       public Node put(Object key,
                                                                                       Node value) {
                                                                           return super
                                                                               .put(key,
                                                                                   new AnchorNode(
                                                                                       value));
                                                                       }
                                                                   };

    protected Object                         objectToRepresent;
    private PropertyUtils                    propertyUtils;
    private boolean                          explicitPropertyUtils = false;

    public Node represent(Object data) {
        Node node = representData(data);
        representedObjects.clear();
        objectToRepresent = null;
        return node;
    }

    protected final Node representData(Object data) {
        objectToRepresent = data;
        // check for identity
        if (representedObjects.containsKey(objectToRepresent)) {
            Node node = representedObjects.get(objectToRepresent);
            return node;
        }
        // }
        // check for null first
        if (data == null) {
            Node node = nullRepresenter.representData(null);
            return node;
        }
        // check the same class
        Node node;
        Class<?> clazz = data.getClass();
        if (representers.containsKey(clazz)) {
            Represent representer = representers.get(clazz);
            node = representer.representData(data);
        } else {
            // check the parents
            for (Class<?> repr : multiRepresenters.keySet()) {
                if (repr.isInstance(data)) {
                    Represent representer = multiRepresenters.get(repr);
                    node = representer.representData(data);
                    return node;
                }
            }

            // check defaults
            if (multiRepresenters.containsKey(null)) {
                Represent representer = multiRepresenters.get(null);
                node = representer.representData(data);
            } else {
                Represent representer = representers.get(null);
                node = representer.representData(data);
            }
        }
        return node;
    }

    protected Node representScalar(Tag tag, String value, Character style) {
        if (style == null) {
            style = this.defaultScalarStyle;
        }
        Node node = new ScalarNode(tag, value, null, null, style);
        return node;
    }

    protected Node representScalar(Tag tag, String value) {
        return representScalar(tag, value, null);
    }

    protected Node representSequence(Tag tag, Iterable<?> sequence, Boolean flowStyle) {
        int size = 10;// default for ArrayList
        if (sequence instanceof List<?>) {
            size = ((List<?>) sequence).size();
        }
        List<Node> value = new ArrayList<Node>(size);
        SequenceNode node = new SequenceNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        boolean bestStyle = true;
        for (Object item : sequence) {
            Node nodeItem = representData(item);
            if (!((nodeItem instanceof ScalarNode && ((ScalarNode) nodeItem).getStyle() == null))) {
                bestStyle = false;
            }
            value.add(nodeItem);
        }
        if (flowStyle == null) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle.getStyleBoolean());
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }

    protected Node representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle) {
        List<NodeTuple> value = new ArrayList<NodeTuple>(mapping.size());
        MappingNode node = new MappingNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        boolean bestStyle = true;
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            Node nodeKey = representData(entry.getKey());
            Node nodeValue = representData(entry.getValue());
            if (!((nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).getStyle() == null))) {
                bestStyle = false;
            }
            if (!((nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).getStyle() == null))) {
                bestStyle = false;
            }
            value.add(new NodeTuple(nodeKey, nodeValue));
        }
        if (flowStyle == null) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle.getStyleBoolean());
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }

    public void setDefaultScalarStyle(ScalarStyle defaultStyle) {
        this.defaultScalarStyle = defaultStyle.getChar();
    }

    public void setDefaultFlowStyle(FlowStyle defaultFlowStyle) {
        this.defaultFlowStyle = defaultFlowStyle;
    }

    public FlowStyle getDefaultFlowStyle() {
        return this.defaultFlowStyle;
    }

    public void setPropertyUtils(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
        this.explicitPropertyUtils = true;
    }

    public final PropertyUtils getPropertyUtils() {
        if (propertyUtils == null) {
            propertyUtils = new PropertyUtils();
        }
        return propertyUtils;
    }

    public final boolean isExplicitPropertyUtils() {
        return explicitPropertyUtils;
    }
}
