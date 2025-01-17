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

import com.github.loadup.commons.error.CommonException;
import com.github.loadup.commons.result.Result;
import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.commons.template.ServiceTemplate;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.commons.util.ValidateUtils;
import com.github.loadup.components.gateway.common.convertor.ClientConfigConvertor;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.api.ClientManageService;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import com.github.loadup.components.gateway.facade.model.ClientConfigDto;
import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("gatewayClientManageService")
public class ClientManageServiceImpl implements ClientManageService {

	private static final Logger logger = LoggerFactory
			.getLogger(ClientManageServiceImpl.class);

	/**
	 * get repository service from extension
	 */
	private RepositoryServiceExtPt getRepositoryService() {
		RepositoryServiceExtPt result = null;// ExtensionPointLoader.get(RepositoryServiceExt.class,
		//            SystemParameter.getParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE));
		return result;
	}

	@Override
	public ClientConfigAddResponse add(ClientConfigAddRequest request) {
		return ServiceTemplate.execute(
				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					String clientId = getRepositoryService()
							.addClient(ClientConfigConvertor.addRequest2Dto(request));
					ClientConfigAddResponse response = new ClientConfigAddResponse();
					response.setClientId(clientId);
					return response;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
				// compose digest log
				(Void) -> {
				}
		);
	}

	@Override
	public ClientConfigAuthorizeResponse authorize(ClientConfigAuthorizeRequest request) {
		ClientConfigAuthorizeResponse authorizeResponse = new ClientConfigAuthorizeResponse();
		return ServiceTemplate.execute(

				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					getRepositoryService()
							.authorizeClient(ClientConfigConvertor.authorizeRequest2Dto(request));

					return authorizeResponse;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),

				// compose digest log
				(Void) -> {

				});
	}

	@Override
	public ClientConfigDeauthorizeResponse deauthorize(ClientConfigDeauthorizeRequest request) {
		ClientConfigDeauthorizeResponse authorizeResponse = new ClientConfigDeauthorizeResponse();
		return ServiceTemplate.execute(
				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					getRepositoryService()
							.deauthorizeClient(ClientConfigConvertor.deauthorizeRequest2Dto(request));

					return authorizeResponse;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
				// compose digest log
				(Void) -> {
				});
	}

	@Override
	public ClientConfigUpdateResponse update(ClientConfigUpdateRequest request) {
		ClientConfigUpdateResponse authorizeResponse = new ClientConfigUpdateResponse();

		return ServiceTemplate.execute(
				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					getRepositoryService().updateClient(ClientConfigConvertor.updateRequest2Dto(request));

					return authorizeResponse;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
				// compose digest log
				(Void) -> {
				});
	}

	@Override
	public ClientConfigQueryResponse query(ClientConfigQueryRequest request) {
		ClientConfigQueryResponse response = new ClientConfigQueryResponse();
		return ServiceTemplate.execute(
				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					String clientId = request.getClientId();
					ClientConfigDto load = getRepositoryService().queryClient(clientId);
					if (load != null) {
						response.setClientId(load.getClientId());
						response.setName(load.getName());
						String properties = load.getProperties();
						if (!StringUtils.isEmpty(properties)) {
							response.setProperties(JsonUtil.jsonToMap(properties));
						}
					}
					return response;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
				// compose digest log
				(Void) -> {
				});
	}

	@Override
	public ClientConfigRemoveResponse remove(ClientConfigRemoveRequest request) {
		ClientConfigRemoveResponse response = new ClientConfigRemoveResponse();
		return ServiceTemplate.execute(
				// check parameter
				(Void) -> ValidateUtils.validate(request),
				// process
				() -> {
					String clientId = request.getClientId();
					getRepositoryService().removeClient(clientId);
					return response;
				},
				// compose exception response
				(e) -> Result.buildFailure(GatewayErrorCode.UNKNOWN_EXCEPTION),
				// compose digest log
				(Void) -> {
				});

	}
}
