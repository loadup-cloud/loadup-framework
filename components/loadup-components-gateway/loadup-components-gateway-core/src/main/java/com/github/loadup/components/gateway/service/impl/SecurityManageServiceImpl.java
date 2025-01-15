package com.github.loadup.components.gateway.service.impl;

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

import com.github.loadup.components.gateway.common.convertor.SecurityManageConvertor;
import com.github.loadup.components.gateway.facade.api.SecurityManageService;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import com.github.loadup.components.gateway.facade.model.SecurityConfigDto;
import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;
import com.github.loadup.components.gateway.service.template.ServiceCallback;
import com.github.loadup.components.gateway.service.template.ServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component("gatewaySecurityManageService")
public class SecurityManageServiceImpl implements SecurityManageService {

	private static final Logger logger = LoggerFactory
			.getLogger(SecurityManageServiceImpl.class);

	private RepositoryServiceExtPt getRepositoryService() {
		RepositoryServiceExtPt result = null;// ExtensionPointLoader.get(RepositoryServiceExt.class,
		//            SystemParameter.getParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE));
		return result;
	}

	@Override
	public CertConfigAddResponse add(CertConfigAddRequest request) {
		CertConfigAddResponse response = new CertConfigAddResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {
			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService()
						.addSecurity(SecurityManageConvertor.certConfigAddRequest2Dto(request));
				response.setAlgoName(request.getClientId());
				response.setOperationType(request.getOperateType());
				response.setClientId(request.getClientId());
				response.setSecurityStrategyCode(request.getSecurityStrategyCode());
			}
		});
		return response;
	}

	@Override
	public CertConfigUpdateResponse update(CertConfigUpdateRequest request) {
		CertConfigUpdateResponse response = new CertConfigUpdateResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {
			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService()
						.updateSecurity(SecurityManageConvertor.certConfigUpdateRequest2Dto(request));
			}
		});
		return response;
	}

	@Override
	public CertConfigQueryResponse query(CertConfigQueryRequest request) {
		CertConfigQueryResponse queryResponse = new CertConfigQueryResponse();
		ServiceTemplate.execute(request, queryResponse, new ServiceCallback() {
			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				List<SecurityConfigDto> queryResult = getRepositoryService()
						.querySecurityByClient(request.getClientId());
				List<CertConfigInnerResponse> configInnerResponses = new ArrayList<>();
				if (!CollectionUtils.isEmpty(queryResult)) {
					for (SecurityConfigDto securityConfigDto : queryResult) {
						configInnerResponses.add(SecurityManageConvertor
								.convertToCertConfigInnerResponse(securityConfigDto));
					}
					queryResponse.setCertConfigList(configInnerResponses);
				}
			}
		});
		return queryResponse;
	}

	@Override
	public CertConfigRemoveResponse remove(CertConfigRemoveRequest request) {
		CertConfigRemoveResponse response = new CertConfigRemoveResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {
			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService()
						.removeSecurity(SecurityManageConvertor.certConfigRemoveRequest2Dto(request));
			}
		});
		return response;
	}

}
