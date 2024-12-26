package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.facade.config.model.APIConditionGroup;
import com.github.loadup.components.gateway.facade.config.model.Constant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
public class APIConditionConfigIndexExtImpl {

    public List<List<String>> customIndexKeys(String tntInstId, String configName,
                                              APIConditionGroup apiConditionGroup) {

        List<List<String>> result = new ArrayList<List<String>>();
        List<String> urlIndex = new ArrayList<>();
        urlIndex.add(Constant.URL_INDEX_COLUMN);
        result.add(urlIndex);

        List<String> integrationUrlIndex = new ArrayList<>();
        integrationUrlIndex.add(Constant.INTEGRATION_URL_INDEX_COLUMN);
        result.add(integrationUrlIndex);
        return result;
    }
}