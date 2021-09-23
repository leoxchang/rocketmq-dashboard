package org.apache.rocketmq.dashboard.sms.service;

import org.springframework.stereotype.Component;

/**
 * asr sms service
 *
 * @author lujianfeng
 * @date 2020-09-27
 */
@Component
public class AsrSmsService {

    /**
     * 短信发送接口 TODO
     *
     * @param phone
     * @param content
     * @return <ul>
     * <li> 大于0的数字	提交成功</li>
     * <li> –1	账号未注册</li>
     * <li> –2	其他错误</li>
     * <li> –3	帐号或密码错误</li>
     * <li> –5	余额不足，请充值</li>
     * <li> –6	定时发送时间不是有效的时间格式</li>
     * <li> -7	提交信息末尾未签名，请添加中文的企业签名【 】或内容乱码</li>
     * <li> –8	发送内容需在1到300字之间</li>
     * <li> -9	发送号码为空</li>
     * <li> -10	定时时间不能小于系统当前时间</li>
     * <li> -100	IP黑名单</li>
     * <li> -102	账号黑名单</li>
     * <li> -103	IP未导白</li>
     * </ul>
     * @author lujianfeng
     * @date 2020/9/27
     */
    public String sendSms(String phone, String content) {

        return null;
    }
}