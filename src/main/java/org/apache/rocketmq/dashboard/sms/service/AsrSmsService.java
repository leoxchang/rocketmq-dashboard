package org.apache.rocketmq.dashboard.sms.service;

import org.apache.rocketmq.dashboard.sms.constant.AsrSmsCodes;
import org.apache.rocketmq.dashboard.sms.constant.InspurProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * asr sms service
 *
 * @author lujianfeng
 * @date 2020-09-27
 */
@Component
public class AsrSmsService {

    /**
     * 短信发送接口
     *
     * @param code    com.asr.hedu.common.sms.constant.AsrSmsCodes
     * @param smsData 验证码数据
     * @param phone
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
    public String sendSms(AsrSmsCodes code, String phone, String... smsData) {
        //组装请求数据
        MultiValueMap<String, String> smsObject = new LinkedMultiValueMap<>();
        smsObject.set("CorpID", InspurProperties.Sms.SMS_KEY);
        smsObject.set("Pwd", InspurProperties.Sms.SMS_PWD);
        String content = code.getSmsContent();
        if (smsData != null && smsData.length > 0) {
            //sms信息单占位符替换，如果多占位符，需要修改为map进行匹配
            String regex = "\\$\\{(\\w+)\\}";
            Matcher m = Pattern.compile(regex).matcher(content);
            int i = 0;
            while (m.find()) {
                content = content.replace(m.group(), smsData[i]);
                i++;
            }
        }
        smsObject.set("Content", content);
        smsObject.set("Mobile", phone);
        RestTemplate restTemplate = new RestTemplate(new HttpsClientRequestFactory());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> reqEntity = new HttpEntity<>(smsObject, headers);
        ResponseEntity<String> en = restTemplate.postForEntity(InspurProperties.SEND_SMS_URL, reqEntity, String.class);
        if (en.getStatusCode() == HttpStatus.OK) {
            if (Integer.parseInt(en.getBody()) > 0) {
                return "OK";
            } else {
                return AsrSmsUtil.getAsrSmsReturn(en.getBody()).getErrorMsg();
            }
        }
        throw new RuntimeException("sms 发送失败！code : [" + en.getStatusCodeValue() + "] , content : [" + en.getBody() + "]");
    }

    /**
     * 短信余额查询接口
     *
     * @param
     * @return java.lang.String
     * @author lujianfeng
     * @date 2020/9/27
     */
    public String checkBalance() {
        RestTemplate restTemplate = new RestTemplate(new HttpsClientRequestFactory());
        Map<String, String> checkObject = new HashMap<>();
        checkObject.put("CorpID", InspurProperties.Sms.SMS_KEY);
        checkObject.put("Pwd", InspurProperties.Sms.SMS_PWD);
        ResponseEntity<String> en = restTemplate.getForEntity(InspurProperties.QUERY_SMS_URL, String.class, checkObject);
        if (en.getStatusCode() == HttpStatus.OK) {
            return en.getBody();
        }
        throw new RuntimeException("sms 余额查询失败！code : [" + en.getStatusCodeValue() + "] , content : [" + en.getBody() + "]");
    }
}