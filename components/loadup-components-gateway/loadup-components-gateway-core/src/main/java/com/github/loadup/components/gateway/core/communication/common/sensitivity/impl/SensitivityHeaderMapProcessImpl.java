/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.communication.common.sensitivity.impl;

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

import com.github.loadup.components.gateway.core.communication.common.sensitivity.SensitivityDataProcess;
import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;
import com.github.loadup.components.gateway.core.prototype.util.MaskUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("sensitivityHeaderMapProcessImpl")
public class SensitivityHeaderMapProcessImpl implements SensitivityDataProcess<Map<String, String>> {
    @Override
    public Map<String, String> mask(Map<String, String> maskContent, Map<String, ShieldType> shieldRule) {

        Map<String, String> result = new HashMap<>();

        maskContent.forEach((key, value) -> {
            if (shieldRule.containsKey(key) && StringUtils.isNotBlank(value)) {
                ShieldType shieldType = shieldRule.get(key);
                String mask = MaskUtil.mask(value, shieldType.name());
                result.put(key, mask);
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    @Override
    public SensitivityProcessType getTag() {
        return SensitivityProcessType.HEADER_MAP;
    }
}
