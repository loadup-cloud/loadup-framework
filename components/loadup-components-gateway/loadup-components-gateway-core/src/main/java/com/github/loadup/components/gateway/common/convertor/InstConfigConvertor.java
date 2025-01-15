package com.github.loadup.components.gateway.common.convertor;

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
