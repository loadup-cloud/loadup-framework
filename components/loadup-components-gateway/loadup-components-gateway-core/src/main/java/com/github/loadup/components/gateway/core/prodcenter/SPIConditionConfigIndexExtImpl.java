package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.facade.config.model.Constant;
import com.github.loadup.components.gateway.facade.config.model.SPIConditionGroup;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *
 */
@Component
public class SPIConditionConfigIndexExtImpl {

    public List<List<String>> customIndexKeys(String tntInstId, String configName,
                                              SPIConditionGroup spiConditionGroup) {
        List<String> spiConditionGroupIndex = new ArrayList<>();
        spiConditionGroupIndex.add(Constant.INTEGRATION_URL_INDEX_COLUMN);
        return new ArrayList<List<String>>(Collections.singleton(spiConditionGroupIndex));
    }
}