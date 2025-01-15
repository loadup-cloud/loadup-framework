package com.github.loadup.components.gateway.common.util;

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

import com.github.loadup.components.gateway.core.common.Constant;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class UriUtil {

	/**
	 * get uri with path separator replaced by dot.
	 * such as:
	 * input: http://query/rate.htm
	 * return: query.rate.htm
	 */
	public static String getUriWithDot(String url) {
		return StringUtils.replace(getURIPath(url), Constant.PATH_SEPARATOR, Constant.PATH_CONJUNCTION);
	}

	/**
	 * get uri
	 */
	public static String getURIPath(String url) {
		int index = StringUtils.indexOf(url, Constant.URI_SEPARATOR);
		if (index >= 0 && index < (url.length() - 3)) {
			return StringUtils.substring(url, index + 3);
		} else {
			return url;
		}
	}

}
