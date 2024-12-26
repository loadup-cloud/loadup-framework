package com.github.loadup.components.gateway.core.communication.common.sensitivity.impl;

import com.github.loadup.components.gateway.core.communication.common.sensitivity.SensitivityDataProcess;
import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;
import com.github.loadup.components.gateway.core.prototype.util.MaskUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Component("sensitivityHeaderMapProcessImpl")
public class SensitivityHeaderMapProcessImpl implements
        SensitivityDataProcess<Map<String, String>> {
    @Override
    public Map<String, String> mask(Map<String, String> maskContent,
                                    Map<String, ShieldType> shieldRule) {

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