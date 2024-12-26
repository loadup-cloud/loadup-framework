package com.github.loadup.components.gateway.service.impl;

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