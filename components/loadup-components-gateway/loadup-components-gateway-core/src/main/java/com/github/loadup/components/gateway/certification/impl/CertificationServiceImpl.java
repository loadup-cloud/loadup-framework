package com.github.loadup.components.gateway.certification.impl;

import com.github.loadup.components.gateway.cache.manager.CertAlgorithmCacheManager;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.facade.CertificationService;
import com.github.loadup.components.gateway.certification.manager.AlgoManager;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全组件服务实现
 */
@Component
public class CertificationServiceImpl implements CertificationService {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory.getLogger("CERT-SERVICE");

    /**
     * 扩展点名称
     */
    private String processComponent;

    /**
     * cert cache manager
     */
    @Resource
    private CertAlgorithmCacheManager certAlgorithmCacheManager;

    /**
     * 算法管理类，通过算法的类别进行管理
     */
    public static Map<String, AlgoManager> algoManagerMap = new HashMap<String, AlgoManager>();

    /**
     * 获取证书内容, 从缓存获取证书内容，证书内容以byte[]的base64Encode后的String格式
     *
     * @throws CertificationException
     */
    @Override
    public String getCert(String securityStrategyCode,
                          String securityStrategyOperateType,
                          String securityStrategyAlgorithm,
                          String clientId)
            throws CertificationException {
        return certAlgorithmCacheManager.getCertContentString(securityStrategyCode,
                securityStrategyOperateType, securityStrategyAlgorithm, clientId);
    }

    //@Override
    //public void registerExtension(Extension extension) throws Exception {
    //    Object[] contribs = extension.getContributions();
    //    if (contribs.length == 0) {
    //        LogUtil.error(logger, "没有扩展信息注册, extension=" + extension);
    //        return;
    //    }
    //
    //    String extensionPoint = extension.getExtensionPoint();
    //
    //    if (logger.isInfoEnabled()) {
    //        LogUtil.info(logger, "注册扩展点extensionPoint=" + extensionPoint + "processComponent = "
    //                + processComponent);
    //    }
    //
    //    if (StringUtils.equals(extensionPoint, processComponent)) {
    //        for (Object contrib : contribs) {
    //            try {
    //                AlgoManagerDescriptor desc = (AlgoManagerDescriptor) contrib;
    //                algoManagerMap.put(desc.getName(), desc.getAlgoManager());
    //            } catch (Exception ex) {
    //                LogUtil.error(logger, "注册扩展点的时候发生异常:contrib=" + contrib, ex);
    //            }
    //
    //            if (logger.isInfoEnabled()) {
    //                LogUtil.info(logger, "注册任务信息:descriptor = " + contrib);
    //            }
    //        }
    //    } else {
    //        LogUtil.error(logger, "没有找到AlgoManagerDescriptor对应的扩展点处理方式，descriptor=" + contribs);
    //    }
    //}

    /**
     * 注册算法管理类
     */
    public static void registerManager(String managerType, AlgoManager manager) {
        if (managerType != null && manager != null) {
            LogUtil.info(logger, "register algo manager：" + managerType);
            algoManagerMap.put(managerType, manager);
        }
    }

    /**
     * Setter method for property <tt>processComponent<tt>.
     */
    public void setProcessComponent(String processComponent) {
        this.processComponent = processComponent;
    }

}