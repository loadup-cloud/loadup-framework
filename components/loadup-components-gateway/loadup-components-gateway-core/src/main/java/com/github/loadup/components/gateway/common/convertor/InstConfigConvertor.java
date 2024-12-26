package com.github.loadup.components.gateway.common.convertor;

import com.github.loadup.components.gateway.certification.util.CommonUtil;
import com.github.loadup.components.gateway.core.model.InstConfig;
import com.github.loadup.components.gateway.facade.model.InstConfigDto;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class InstConfigConvertor {

    /**
     * convert dto to domain model
     */
    public static InstConfig dto2Model(InstConfigDto item) {
        InstConfig model = new InstConfig();
        model.setName(item.getName());
        model.setClientId(item.getClientId());
        model.getProperties().setProperties(CommonUtil.Str2Kv(item.getProperties()));
        return model;
    }

    /**
     * convert dto list to domain model list
     */
    public static List<InstConfig> dtoList2ModelList(List<InstConfigDto> instConfigDtos) {
        List<InstConfig> models = new ArrayList<>();
        if (CollectionUtils.isEmpty(instConfigDtos)) {
            return models;
        }
        for (InstConfigDto dto : instConfigDtos) {
            models.add(dto2Model(dto));
        }
        return models;
    }
}