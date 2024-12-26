package com.github.loadup.components.gateway.cache.manager;

import com.github.loadup.components.gateway.core.model.ShieldConfig;
import com.github.loadup.components.gateway.core.model.ShieldType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * <p>
 * SensitivityManager.java
 */
@Component("gateway.cache.manager.sensitivityManager")
public class SensitivityManager {

    @Value("${shieldKeys:mobile,bankcard,id}")
    private String shieldKeys;

    public void init() {
        Arrays.asList(StringUtils.split(shieldKeys, ",")).stream().forEach(key -> {
            ShieldConfig.putShiledConfig(key, ShieldType.ALL);
        });
    }
}