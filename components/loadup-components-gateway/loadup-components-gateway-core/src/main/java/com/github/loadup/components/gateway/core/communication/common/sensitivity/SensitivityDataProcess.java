package com.github.loadup.components.gateway.core.communication.common.sensitivity;

import com.github.loadup.components.gateway.core.model.SensitivityProcessType;
import com.github.loadup.components.gateway.core.model.ShieldType;

import java.util.Map;

/**
 *
 */
public interface SensitivityDataProcess<T> {

    /**
     *
     */
    T mask(T maskContent, Map<String, ShieldType> shieldRule);

    SensitivityProcessType getTag();

}