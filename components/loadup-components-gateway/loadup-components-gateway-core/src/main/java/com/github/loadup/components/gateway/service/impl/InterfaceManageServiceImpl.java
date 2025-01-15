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

import com.github.loadup.components.gateway.common.convertor.InterfaceConfigConvertor;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.api.InterfaceManageService;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import com.github.loadup.components.gateway.facade.model.InterfaceDto;
import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;
import com.github.loadup.components.gateway.service.template.ServiceCallback;
import com.github.loadup.components.gateway.service.template.ServiceTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component("gatewayInterfaceManageService")
public class InterfaceManageServiceImpl implements InterfaceManageService {

	@Override
	public InterfaceConfigAddResponse add(InterfaceConfigAddRequest request) {
		InterfaceConfigAddResponse interfaceConfigAddResponse = new InterfaceConfigAddResponse();
		ServiceTemplate.execute(request, interfaceConfigAddResponse, new ServiceCallback() {
			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				InterfaceDto interfaceAddConfigDto = InterfaceConfigConvertor.convertToDto(request);
				getRepositoryService().addInterface(interfaceAddConfigDto);
				interfaceConfigAddResponse.setInterfaceId(interfaceAddConfigDto.getInterfaceId());
			}

		});

		return interfaceConfigAddResponse;
	}

	@Override
	public InterfaceConfigUpdateResponse update(InterfaceConfigUpdateRequest request) {
		InterfaceConfigUpdateResponse interfaceConfigUpdateResponse = new InterfaceConfigUpdateResponse();
		ServiceTemplate.execute(request, interfaceConfigUpdateResponse, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				InterfaceDto interfaceDto = InterfaceConfigConvertor.convertToDto(request);
				getRepositoryService().updateInterface(interfaceDto);
				interfaceConfigUpdateResponse.setInterfaceId(request.getInterfaceId());
			}

		});

		return interfaceConfigUpdateResponse;
	}

	@Override
	public InterfaceConfigUpgradeResponse upgrade(InterfaceConfigUpgradeRequest request) {
		InterfaceConfigUpgradeResponse response = new InterfaceConfigUpgradeResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				InterfaceDto interfaceDto = InterfaceConfigConvertor.convertToDto(request);
				getRepositoryService().upgradeInterface(interfaceDto);
				response.setInterfaceId(interfaceDto.getInterfaceId());
				response.setVersion(interfaceDto.getVersion());
			}

		});

		return response;
	}

	@Override
	public InterfaceConfigQueryResponse query(InterfaceConfigQueryRequest request) {
		InterfaceConfigQueryResponse response = new InterfaceConfigQueryResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
				request
						.setPage(request.getPage() == null ? Constant.DEFAULT_PAGE : request.getPage());
				request.setPageSize(request.getPageSize() == null ? Constant.DEFAULT_PAGE_SIZE
						: request.getPageSize());
			}

			@Override
			public void process() {
				List<InterfaceDto> dto = getRepositoryService().queryInterface(
						request.getPageSize(), request.getPage(), request.getTntInstId(),
						request.getInterfaceId(), request.getClientId(), request.getType(),
						request.getStatus(), request.getInterfaceName());
				response
						.setInterfaceConfigList(InterfaceConfigConvertor.buildinterfaceConfigList(dto));
			}

		});

		return response;
	}

	@Override
	public InterfaceConfigRemoveResponse remove(InterfaceConfigRemoveRequest request) {
		InterfaceConfigRemoveResponse interfaceConfigRemoveResponse = new InterfaceConfigRemoveResponse();
		ServiceTemplate.execute(request, interfaceConfigRemoveResponse, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService().removeInterface(request.getInterfaceId());
			}

		});
		return interfaceConfigRemoveResponse;
	}

	@Override
	public InterfaceConfigOnlineResponse online(InterfaceConfigOnlineRequest request) {
		InterfaceConfigOnlineResponse response = new InterfaceConfigOnlineResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService().onlineInterface(request.getInterfaceId());
				response.setInterfaceId(request.getInterfaceId());
			}

		});

		return response;
	}

	@Override
	public InterfaceConfigOfflineResponse offline(InterfaceConfigOfflineRequest request) {
		InterfaceConfigOfflineResponse response = new InterfaceConfigOfflineResponse();
		ServiceTemplate.execute(request, response, new ServiceCallback() {

			@Override
			public void checkParameter() {
			}

			@Override
			public void preProcess() {
			}

			@Override
			public void process() {
				getRepositoryService().offlineInterface(request.getInterfaceId());
				response.setInterfaceId(request.getInterfaceId());
			}

		});

		return response;
	}

	private RepositoryServiceExtPt getRepositoryService() {
		RepositoryServiceExtPt result = null;//ExtensionPointLoader.get(RepositoryServiceExt.class,
		//            SystemParameter.getParameter(Constant.REPOSITORY_EXTPOINT_BIZCODE));
		return result;

	}

}
