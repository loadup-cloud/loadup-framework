package com.github.loadup.components.gateway.core.prodcenter;

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