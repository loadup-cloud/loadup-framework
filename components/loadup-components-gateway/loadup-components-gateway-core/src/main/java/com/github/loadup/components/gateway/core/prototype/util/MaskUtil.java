package com.github.loadup.components.gateway.core.prototype.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 屏蔽敏感字段工具
 */
public class MaskUtil {

    private static final Logger logger = LoggerFactory.getLogger(MaskUtil.class);

    /**
     * 屏蔽后的字符串，用来打印敏感日志
     */
    public static String mask(String origStr, String shieldTypeStr) {
        //        ShieldMethodEnum shieldMethodEnum = null;
        //        try {
        //            shieldMethodEnum = ShieldMethodEnum.valueOf(shieldTypeStr);
        //        } catch (Exception e) {
        //            LogUtil.warn(logger, e, "MaskUtil.mask fail! shieldTypeStr is ", shieldTypeStr);
        //            shieldMethodEnum = ShieldMethodEnum.ALL;
        //        }
        //
        //        Shield shield = ShieldFactory.getShielder(shieldMethodEnum);
        //
        //        return shield.shield(origStr, new String[] {});
        return "";
    }

    /**
     * 屏蔽前6后4
     */
    public static String shieldTopSixLastFour(String origStr) {
        //eg: cardno should be shielded in log
        if (origStr.length() <= 10) {
            return defaultHide(origStr);
        }
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(origStr.substring(0, 6)).append("****")
                .append(origStr.substring(origStr.length() - 4)).toString();
    }

    /**
     * 默认方式，分成三份，中间加密
     */
    public static String defaultHide(String sensitiveData) {

        return "***";
    }

}
