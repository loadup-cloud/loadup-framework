/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;
import com.github.loadup.components.gateway.facade.enums.OperationType;
import com.github.loadup.components.gateway.facade.model.SecurityConfigDto;
import com.github.loadup.components.gateway.facade.request.CertConfigAddRequest;
import com.github.loadup.components.gateway.facade.request.CertConfigRemoveRequest;
import com.github.loadup.components.gateway.facade.request.CertConfigUpdateRequest;
import com.github.loadup.components.gateway.facade.response.CertConfigInnerResponse;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class SecurityManageConvertor {
    /**
     * CertConfigAddRequest to SecurityConfigDto
     */
    public static SecurityConfigDto certConfigAddRequest2Dto(CertConfigAddRequest request) {
        if (request == null) {
            return null;
        }
        SecurityConfigDto addRequestDto = new SecurityConfigDto();
        addRequestDto.setClientId(request.getClientId());
        addRequestDto.setAlgoName(request.getAlgoName());
        addRequestDto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        addRequestDto.setOperateType(request.getOperateType().getName());
        addRequestDto.setCertType(request.getCertType().getCertType());
        addRequestDto.setCertContent(request.getCertContent());
        addRequestDto.setStatus("VALID");
        addRequestDto.setGmtCreate(new Date());
        addRequestDto.setGmtModified(new Date());
        addRequestDto.setKeyType(request.getKeyType());
        if (request.getProperties() != null) {
            // addRequestDto.setProperties(CommonUtil.kv2Str(request.getProperties()));
            addRequestDto.setProperties(JsonUtil.toJSONString(request.getProperties()));
        }
        if (request.getAlgorithmProperties() != null) {
            // addRequestDto.setAlgoProperties(CommonUtil.kv2Str(request.getAlgorithmProperties()));
            addRequestDto.setAlgoProperties(JsonUtil.toJSONString(request.getAlgorithmProperties()));
        }
        return addRequestDto;
    }

    /**
     * SecurityConfigDto to CertConfigInnerResponse
     */
    public static CertConfigInnerResponse convertToCertConfigInnerResponse(SecurityConfigDto innerDto) {
        if (innerDto == null) {
            return null;
        }
        CertConfigInnerResponse innerResponse = new CertConfigInnerResponse();
        innerResponse.setAlgoName(innerDto.getAlgoName());
        innerResponse.setSecurityStrategyCode(innerDto.getSecurityStrategyCode());
        innerResponse.setClientId(innerDto.getClientId());
        innerResponse.setOperateType(OperationType.getByName(innerDto.getOperateType()));
        innerResponse.setCertContent(innerDto.getCertContent());
        innerResponse.setCertType(CertTypeEnum.getEnumByType(innerDto.getCertType()));
        innerResponse.setKeyType(innerDto.getKeyType());
        innerResponse.setCertStatus(innerDto.getStatus());
        if (!StringUtils.isBlank(innerDto.getProperties())) {
            // innerResponse.setProperties(CommonUtil.Str2Kv(innerDto.getProperties()));
            innerResponse.setProperties(JsonUtil.jsonToMap(innerDto.getProperties()));
        }
        if (!StringUtils.isBlank(innerDto.getAlgoProperties())) {
            // innerResponse.setAlgorithmProperties(CommonUtil.Str2Kv(innerDto.getAlgoProperties()));
            innerResponse.setAlgorithmProperties(JsonUtil.jsonToMap(innerDto.getAlgoProperties()));
        }
        return innerResponse;
    }

    /**
     * CertConfigRemoveRequest to SecurityConfigDto
     */
    public static SecurityConfigDto certConfigRemoveRequest2Dto(CertConfigRemoveRequest request) {
        if (request == null) {
            return null;
        }
        SecurityConfigDto certConfigReomveRequestDto = new SecurityConfigDto();
        certConfigReomveRequestDto.setAlgoName(request.getAlgoName());
        certConfigReomveRequestDto.setClientId(request.getClientId());
        certConfigReomveRequestDto.setOperateType(request.getOperateType().getName());
        certConfigReomveRequestDto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        return certConfigReomveRequestDto;
    }

    /**
     * CertConfigUpdateRequest to SecurityConfigDto
     */
    public static SecurityConfigDto certConfigUpdateRequest2Dto(CertConfigUpdateRequest request) {
        if (request == null) {
            return null;
        }
        SecurityConfigDto updateRequestDto = new SecurityConfigDto();
        updateRequestDto.setSecurityStrategyCode(request.getSecurityStrategyCode());
        updateRequestDto.setClientId(request.getClientId());
        updateRequestDto.setAlgoName(request.getAlgoName());
        updateRequestDto.setOperateType(request.getOperateType().getName());
        updateRequestDto.setCertType(request.getCertType().getCertType());
        updateRequestDto.setCertContent(request.getCertContent());
        updateRequestDto.setKeyType(request.getKeyType());
        updateRequestDto.setGmtModified(new Date());
        if (request.getProperties() != null) {
            updateRequestDto.setProperties(JsonUtil.toJSONString(request.getProperties()));
        }
        if (request.getAlgorithmProperties() != null) {
            updateRequestDto.setAlgoProperties(JsonUtil.toJSONString(request.getAlgorithmProperties()));
        }
        return updateRequestDto;
    }
}
