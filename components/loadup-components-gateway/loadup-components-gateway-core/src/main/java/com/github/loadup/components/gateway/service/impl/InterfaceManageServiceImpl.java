package com.github.loadup.components.gateway.service.impl;

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