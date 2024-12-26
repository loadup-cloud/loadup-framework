package com.github.loadup.components.gateway.core.communication.common.sensitivity.impl;

import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 敏感Form数据处理
 */
@Component("sensitivityFormProcessImpl")
public class SensitivityFormProcessImpl extends SensitivityUrlProcessImpl {

    @Override
    public String mask(String maskContent, Map<String, ShieldType> shieldRule) {
        return super.mask(maskContent, shieldRule);
    }

    @Override
    public SensitivityProcessType getTag() {
        return SensitivityProcessType.FORM_BODY;
    }
}
