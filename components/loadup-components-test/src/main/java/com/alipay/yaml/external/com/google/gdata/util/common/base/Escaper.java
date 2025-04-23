/* Copyright (c) 2008 Google Inc.
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

package com.alipay.yaml.external.com.google.gdata.util.common.base;

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

/**
 * An object that converts literal text into a format safe for inclusion in a
 * particular context (such as an XML document). Typically (but not always), the
 * inverse process of "unescaping" the text is performed automatically by the
 * relevant parser.
 * 
 * <p>
 * For example, an XML escaper would convert the literal string
 * {@code "Foo<Bar>"} into {@code "Foo&lt;Bar&gt;"} to prevent {@code "<Bar>"}
 * from being confused with an XML tag. When the resulting XML document is
 * parsed, the parser API will return this text as the original literal string
 * {@code "Foo<Bar>"}.
 * 
 * <p>
 * An {@code Escaper} instance is required to be stateless, and safe when used
 * concurrently by multiple threads.
 * 
 * <p>
 * Several popular escapers are defined as constants in the class
 * {@link CharEscapers}. To create your own escapers, use
 * {@link CharEscaperBuilder}, or extend {@link CharEscaper} or
 * {@code UnicodeEscaper}.
 * 
 * 
 */
public interface Escaper {
    /**
     * Returns the escaped form of a given literal string.
     * 
     * <p>
     * Note that this method may treat input characters differently depending on
     * the specific escaper implementation.
     * <ul>
     * <li>{@link UnicodeEscaper} handles <a
     * href="http://en.wikipedia.org/wiki/UTF-16">UTF-16</a> correctly,
     * including surrogate character pairs. If the input is badly formed the
     * escaper should throw {@link IllegalArgumentException}.
     * <li>{@link CharEscaper} handles Java characters independently and does
     * not verify the input for well formed characters. A CharEscaper should not
     * be used in situations where input is not guaranteed to be restricted to
     * the Basic Multilingual Plane (BMP).
     * </ul>
     * 
     * @param string
     *            the literal string to be escaped
     * @return the escaped form of {@code string}
     * @throws NullPointerException
     *             if {@code string} is null
     * @throws IllegalArgumentException
     *             if {@code string} contains badly formed UTF-16 or cannot be
     *             escaped for any other reason
     */
    public String escape(String string);

    /**
     * Returns an {@code Appendable} instance which automatically escapes all
     * text appended to it before passing the resulting text to an underlying
     * {@code Appendable}.
     * 
     * <p>
     * Note that this method may treat input characters differently depending on
     * the specific escaper implementation.
     * <ul>
     * <li>{@link UnicodeEscaper} handles <a
     * href="http://en.wikipedia.org/wiki/UTF-16">UTF-16</a> correctly,
     * including surrogate character pairs. If the input is badly formed the
     * escaper should throw {@link IllegalArgumentException}.
     * <li>{@link CharEscaper} handles Java characters independently and does
     * not verify the input for well formed characters. A CharEscaper should not
     * be used in situations where input is not guaranteed to be restricted to
     * the Basic Multilingual Plane (BMP).
     * </ul>
     * 
     * @param out
     *            the underlying {@code Appendable} to append escaped output to
     * @return an {@code Appendable} which passes text to {@code out} after
     *         escaping it.
     */
    public Appendable escape(Appendable out);
}
