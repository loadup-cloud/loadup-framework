package com.github.loadup.components.gateway.facade.extpoint;

import com.alibaba.cola.extension.Extension;
import com.github.loadup.components.gateway.facade.enums.CertTypeEnum;

import java.io.IOException;

/**
 * Extension Certification access service, this service can be implemented to fetch the
 * certification content through the different ways.
 */
@Extension
public interface CertificationAccessExt {

    /**
     * <p>
     * get the certification content
     * </p>
     *
     * @throws IOException io exception
     */
    String getCertContent(String certAliasName, CertTypeEnum certType) throws IOException;
}
