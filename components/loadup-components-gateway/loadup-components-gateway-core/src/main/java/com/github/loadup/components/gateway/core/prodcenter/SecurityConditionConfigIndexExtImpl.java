package com.github.loadup.components.gateway.core.prodcenter;

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

import com.github.loadup.components.gateway.facade.config.model.Constant;
import com.github.loadup.components.gateway.facade.config.model.SecurityConditionGroup;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * security index build class
 */
@Component
public class SecurityConditionConfigIndexExtImpl {

	public List<List<String>> customIndexKeys(String tntInstId, String configName,
											SecurityConditionGroup securityConditionGroup) {
		List<String> securityConditionGroupIndex = new ArrayList<>();
		securityConditionGroupIndex.add(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_CODE);
		securityConditionGroupIndex
				.add(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_OPERATE_TYPE);
		securityConditionGroupIndex.add(Constant.SECURITY_CONDITION_GROUP_INDEX_COLUMN_ALGO);
		return new ArrayList<List<String>>(Collections.singleton(securityConditionGroupIndex));
	}
}
