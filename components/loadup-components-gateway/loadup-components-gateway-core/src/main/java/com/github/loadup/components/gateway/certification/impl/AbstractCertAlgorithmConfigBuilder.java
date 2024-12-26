package com.github.loadup.components.gateway.certification.impl;

import com.github.loadup.components.gateway.certification.model.OperationType;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractCertAlgorithmConfigBuilder<T> {

    private static Logger logger = LoggerFactory.getLogger(AbstractCertAlgorithmConfigBuilder.class);

    private final static String SECURITY_STRATEGY_CODE = "security_strategy_code";

    private final static String SECURITY_STRATEGY_OPERATE_TYPE = "security_strategy_operate_type";

    private final static String SECURITY_STRATEGY_ALGORITHM = "security_strategy_algorithm";

    private final static String SECURITY_STRATEGY_KEY_TYPE = "security_strategy_key_type";

    private final static String CERT_TYPE = "cert_type";

    private final static String SECURITY_STRATEGY_KEY = "security_strategy_key";

    private final static String CLIENT_ID = "client_id";

    /**
     * validate mandatory fields
     * <p>
     * format should follow the template which is defined in SECURITY_STRATEGY_CONF
     * 0 security_strategy_code, Mandatory
     * 1 security_strategy_operate_type, Mandatory
     * 2 security_strategy_algorithm, Mandatory
     * 3 security_strategy_key_type, Mandatory
     * 4 cert_type, Mandatory
     * 5 security_strategy_key, Mandatory
     * 6 client_id,
     * 7 cert_properties,
     * 8 algorithm_properties
     */
    protected boolean validate(String... fileColumns) {
        List<String> invalidFieldList = new ArrayList<String>(7);
        if (StringUtils.isBlank(fileColumns[0])) {
            invalidFieldList.add(SECURITY_STRATEGY_CODE);
        }

        if (StringUtils.isBlank(fileColumns[1])) {
            invalidFieldList.add(SECURITY_STRATEGY_OPERATE_TYPE);
        } else if (StringUtils.equals(OperationType.OP_VERIFY.getName(), fileColumns[1]) && StringUtils.isBlank(fileColumns[6])) {
            invalidFieldList.add(CLIENT_ID);
        }

        if (StringUtils.isBlank(fileColumns[2])) {
            invalidFieldList.add(SECURITY_STRATEGY_ALGORITHM);
        }

        if (StringUtils.isBlank(fileColumns[3])) {
            invalidFieldList.add(SECURITY_STRATEGY_KEY_TYPE);
        }

        if (StringUtils.isBlank(fileColumns[4])) {
            invalidFieldList.add(CERT_TYPE);
        }

        if (StringUtils.isBlank(fileColumns[5])) {
            invalidFieldList.add(SECURITY_STRATEGY_KEY);
        }

        //TODO error msg
        if (CollectionUtils.isNotEmpty(invalidFieldList)) {
            LogUtil.error(logger, "There are some Empty fields=" +
                    StringUtils.join(invalidFieldList.iterator(), Constant.COMMA_SEPARATOR));
            return false;
        } else {
            return true;
        }
    }

}
