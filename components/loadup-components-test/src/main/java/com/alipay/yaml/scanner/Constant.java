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
package com.alipay.yaml.scanner;

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

import java.util.Arrays;

public final class Constant {
    private final static String  ALPHA_S            = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";

    private final static String  LINEBR_S           = "\n\u0085\u2028\u2029";
    private final static String  FULL_LINEBR_S      = "\r" + LINEBR_S;
    private final static String  NULL_OR_LINEBR_S   = "\0" + FULL_LINEBR_S;
    private final static String  NULL_BL_LINEBR_S   = " " + NULL_OR_LINEBR_S;
    private final static String  NULL_BL_T_LINEBR_S = "\t" + NULL_BL_LINEBR_S;
    private final static String  NULL_BL_T_S        = "\0 \t";
    private final static String  URI_CHARS_S        = ALPHA_S + "-;/?:@&=+$,_.!~*\'()[]%";

    public final static Constant LINEBR             = new Constant(LINEBR_S);
    public final static Constant FULL_LINEBR        = new Constant(FULL_LINEBR_S);
    public final static Constant NULL_OR_LINEBR     = new Constant(NULL_OR_LINEBR_S);
    public final static Constant NULL_BL_LINEBR     = new Constant(NULL_BL_LINEBR_S);
    public final static Constant NULL_BL_T_LINEBR   = new Constant(NULL_BL_T_LINEBR_S);
    public final static Constant NULL_BL_T          = new Constant(NULL_BL_T_S);
    public final static Constant URI_CHARS          = new Constant(URI_CHARS_S);

    public final static Constant ALPHA              = new Constant(ALPHA_S);

    private String               content;
    boolean[]                    contains           = new boolean[128];
    boolean                      noASCII            = false;

    private Constant(String content) {
        Arrays.fill(contains, false);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < 128)
                contains[ch] = true;
            else
                sb.append(ch);
        }
        if (sb.length() > 0) {
            noASCII = true;
            this.content = sb.toString();
        }
    }

    public boolean has(char ch) {
        return (ch < 128) ? contains[ch] : noASCII && content.indexOf(ch, 0) != -1;
    }

    public boolean hasNo(char ch) {
        return !has(ch);
    }

    public boolean has(char ch, String additional) {
        return has(ch) || additional.indexOf(ch, 0) != -1;
    }

    public boolean hasNo(char ch, String additional) {
        return !has(ch, additional);
    }
}
