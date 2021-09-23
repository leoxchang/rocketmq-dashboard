package org.apache.rocketmq.dashboard.sms.service;


import org.apache.rocketmq.dashboard.sms.constant.AsrSmsCodes;
import org.apache.rocketmq.dashboard.sms.constant.AsrSmsReturn;

import java.util.Arrays;

/**
 * sms util
 *
 * @author lujianfeng
 * @date 2020-12-18
 */
public class AsrSmsUtil {
    /**
     * 获取AsrSMScodes
     *
     * @param type
     * @return
     */
    public static AsrSmsCodes getAsrSmsCodes(String type) {
        return Arrays.asList(AsrSmsCodes.values())
                .stream()
                .filter(asrSmsCodes -> asrSmsCodes.getType().equals(type))
                .findFirst()
                .orElse(AsrSmsCodes.UNKNOWN);
    }

    /**
     * 获取返回码
     *
     * @param errorCode
     * @return
     */
    public static AsrSmsReturn getAsrSmsReturn(String errorCode) {
        return Arrays.asList(AsrSmsReturn.values())
                .stream()
                .filter(asrSmsReturn -> asrSmsReturn.getErrorCode().equals(errorCode))
                .findFirst()
                .orElse(AsrSmsReturn.OTHER_ERROR);
    }
}