package com.github.loadup.components.gateway.certification.manager;

import com.github.loadup.components.gateway.certification.exception.CertificationErrorCode;
import com.github.loadup.components.gateway.certification.exception.CertificationException;
import com.github.loadup.components.gateway.certification.impl.CertificationServiceImpl;
import com.github.loadup.components.gateway.certification.model.CertificationFactor;
import com.github.loadup.components.gateway.certification.spi.OuterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义算法管理类
 */
@Component
public class SelfDefAlgoManager extends AbstractAlgoManager {

    /**
     * 日志定义
     */
    private static Logger logger = LoggerFactory
            .getLogger(SelfDefAlgoManager.class);

    private Map<String, OuterService> serviceMap = new HashMap<String, OuterService>();

    /**
     * 扩展点名称
     */
    private String processComponent;

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
    //                    + processComponent);
    //    }
    //
    //    if (StringUtils.equals(extensionPoint, processComponent)) {
    //        for (Object contrib : contribs) {
    //            try {
    //                OutServiceDescriptor desc = (OutServiceDescriptor) contrib;
    //                this.serviceMap.put(desc.getName(), desc.getOuterService());
    //            } catch (Exception ex) {
    //                LogUtil.error(logger, "注册扩展点的时候发生异常:contrib=" + contrib, ex);
    //            }
    //
    //            if (logger.isInfoEnabled()) {
    //                LogUtil.info(logger, "注册任务信息:descriptor = " + contrib);
    //            }
    //        }
    //    } else {
    //        LogUtil.error(logger, "没有找到OutServiceDescriptor对应的扩展点处理方式，descriptor=" + contribs);
    //    }
    //}

    /**
     * get outService
     */
    private OuterService getService(String algoString) {
        OuterService service = this.serviceMap.get(algoString);
        if (null == service) {
            throw new CertificationException(CertificationErrorCode.NO_OUT_SERVICE, "no OutService named "
                    + algoString);
        }
        return service;
    }

    /**
     *
     */
    @Override
    public String encrypt(String srcContent, CertificationFactor certificationFactor) {
        return getService(certificationFactor.getAlgoString()).encrypt(srcContent,
                certificationFactor);
    }

    /**
     *
     */
    @Override
    public String decrypt(String encryptedContent, CertificationFactor certificationFactor) {
        return getService(certificationFactor.getAlgoString()).decrypt(encryptedContent,
                certificationFactor);
    }

    /**
     *
     */
    @Override
    public String sign(String srcContent, CertificationFactor certificationFactor) {
        return getService(certificationFactor.getAlgoString())
                .sign(srcContent, certificationFactor);
    }

    /**
     *
     */
    @Override
    public boolean verify(String srcContent, String signedContent,
                          CertificationFactor certificationFactor) {
        return getService(certificationFactor.getAlgoString()).verify(srcContent, signedContent,
                certificationFactor);
    }

    /**
     *
     */
    @Override
    public String digest(String srcContent, CertificationFactor certificationFactor) {
        return getService(certificationFactor.getAlgoString()).digest(srcContent,
                certificationFactor);
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     * as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CertificationServiceImpl.registerManager("SELF_DEF_ALGO", this);
    }

    /**
     * Getter method for property <tt>processComponent<tt>.
     */
    public String getProcessComponent() {
        return processComponent;
    }

    /**
     * Setter method for property <tt>processComponent<tt>.
     */
    public void setProcessComponent(String processComponent) {
        this.processComponent = processComponent;
    }
}