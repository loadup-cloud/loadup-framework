package com.github.loadup.components.gateway.service.impl;

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.common.convertor.ClientConfigConvertor;
import com.github.loadup.components.gateway.facade.api.ClientManageService;
import com.github.loadup.components.gateway.facade.extpoint.RepositoryServiceExtPt;
import com.github.loadup.components.gateway.facade.model.ClientConfigDto;
import com.github.loadup.components.gateway.facade.request.*;
import com.github.loadup.components.gateway.facade.response.*;
import com.github.loadup.components.gateway.service.template.ServiceCallback;
import com.github.loadup.components.gateway.service.template.ServiceTemplate;
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
        ClientConfigAddResponse response = new ClientConfigAddResponse();
        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
            }

            @Override
            public void process() {
                String clientId = getRepositoryService()
                        .addClient(ClientConfigConvertor.addRequest2Dto(request));
                response.setClientId(clientId);

            }
        });
        return response;
    }

    @Override
    public ClientConfigAuthorizeResponse authorize(ClientConfigAuthorizeRequest request) {
        ClientConfigAuthorizeResponse authorizeResponse = new ClientConfigAuthorizeResponse();
        ServiceTemplate.execute(request, authorizeResponse, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
            }

            @Override
            public void process() {
                getRepositoryService()
                        .authorizeClient(ClientConfigConvertor.authorizeRequest2Dto(request));
            }
        });
        return authorizeResponse;
    }

    @Override
    public ClientConfigDeauthorizeResponse deauthorize(ClientConfigDeauthorizeRequest request) {
        ClientConfigDeauthorizeResponse response = new ClientConfigDeauthorizeResponse();
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
                        .deauthorizeClient(ClientConfigConvertor.deauthorizeRequest2Dto(request));
            }
        });
        return response;
    }

    @Override
    public ClientConfigUpdateResponse update(ClientConfigUpdateRequest request) {
        ClientConfigUpdateResponse response = new ClientConfigUpdateResponse();

        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
            }

            @Override
            public void process() {
                getRepositoryService().updateClient(ClientConfigConvertor.updateRequest2Dto(request));
            }
        });
        return response;
    }

    @Override
    public ClientConfigQueryResponse query(ClientConfigQueryRequest request) {
        ClientConfigQueryResponse response = new ClientConfigQueryResponse();

        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
            }

            @Override
            public void process() {
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
            }
        });
        return response;
    }

    @Override
    public ClientConfigRemoveResponse remove(ClientConfigRemoveRequest request) {
        ClientConfigRemoveResponse response = new ClientConfigRemoveResponse();

        ServiceTemplate.execute(request, response, new ServiceCallback() {
            @Override
            public void checkParameter() {
            }

            @Override
            public void preProcess() {
            }

            @Override
            public void process() {
                String clientId = request.getClientId();
                getRepositoryService().removeClient(clientId);
            }
        });
        return response;
    }
}