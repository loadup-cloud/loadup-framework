package io.github.loadup.gateway.plugins;

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class RpcProxyProcessor implements ProxyProcessor {
    private static final Logger log = LoggerFactory.getLogger(RpcProxyProcessor.class);


    private final ApplicationConfig applicationConfig;
    private final RegistryConfig registryConfig;
    private final Map<String, GenericService> serviceCache = new ConcurrentHashMap<>();

    public RpcProxyProcessor(GatewayProperties gatewayProperties) {
        this.applicationConfig = new ApplicationConfig();
        this.applicationConfig.setName("loadup-gateway");

        this.registryConfig = new RegistryConfig();
        String addr = gatewayProperties.getProxyPlugins().getRpc().getRegistryAddress();
        this.registryConfig.setAddress(addr != null ? addr : "nacos://127.0.0.1:8848");

        log.info("RpcProxyProcessor initialized, registry={}", registryConfig.getAddress());
    }

    @Override public String getName() { return "RpcProxyPlugin"; }
    @Override public String getType() { return "PROXY"; }
    @Override public String getVersion() { return "2.0.0"; }
    @Override public int getPriority() { return 300; }
    @Override public void initialize() {}
    @Override public void destroy() { serviceCache.clear(); }
    @Override public String getSupportedProtocol() { return GatewayConstants.Protocol.RPC; }

    @Override
    public GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception {
        String target = route.getTargetUrl();
        String[] parts = target.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid RPC target: " + target);
        }

        String interfaceName = parts[0];
        String methodName = parts[1];
        String version = parts.length > 2 ? parts[2] : null;

        GenericService svc = getGenericService(interfaceName, version);
        Object[] args = prepareRpcArgs(request);
        String[] paramTypes = getParameterTypes(args);

        Object result = svc.$invoke(methodName, paramTypes, args);

        return GatewayResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(GatewayConstants.Status.SUCCESS)
                .headers(new HashMap<>())
                .body(JsonUtil.toJson(result))
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private GenericService getGenericService(String interfaceName, String version) {
        String key = interfaceName + ":" + (version != null ? version : "");
        return serviceCache.computeIfAbsent(key, k -> {
            ReferenceConfig<GenericService> ref = new ReferenceConfig<>();
            ref.setApplication(applicationConfig);
            ref.setRegistry(registryConfig);
            ref.setInterface(interfaceName);
            ref.setGeneric(true);
            if (version != null) ref.setVersion(version);
            return ref.get();
        });
    }

    private Object[] prepareRpcArgs(GatewayRequest request) {
        String body = request.getBody();
        if (body == null || body.isBlank()) return new Object[0];
        if (body.trim().startsWith("[")) return JsonUtil.fromJson(body, Object[].class);
        return new Object[] {JsonUtil.toMap(body)};
    }

    private String[] getParameterTypes(Object[] args) {
        String[] types = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Map) types[i] = "java.util.Map";
            else if (args[i] instanceof String) types[i] = "java.lang.String";
            else types[i] = "java.lang.Object";
        }
        return types;
    }
}
