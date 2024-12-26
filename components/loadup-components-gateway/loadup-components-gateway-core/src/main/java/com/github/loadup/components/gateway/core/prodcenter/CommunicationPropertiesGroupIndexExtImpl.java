package com.github.loadup.components.gateway.core.prodcenter;

import com.github.loadup.components.gateway.facade.config.model.CommunicationPropertiesGroup;
import com.github.loadup.components.gateway.facade.config.model.Constant;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * communication properties group index
 */
@Component
public class CommunicationPropertiesGroupIndexExtImpl {

    public List<List<String>> customIndexKeys(String tntInstId, String configName,
                                              CommunicationPropertiesGroup communicationPropertiesGroup) {

        List<String> indexList = new ArrayList<>();
        indexList.add(Constant.URL_INDEX_COLUMN);
        return new ArrayList<>(Collections.singleton(indexList));
    }
}