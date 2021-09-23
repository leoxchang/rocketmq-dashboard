package org.apache.rocketmq.dashboard.sms.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * inspur properties
 *
 * @author lujianfeng
 * @date 2020-09-27
 */
@Component
public class InspurProperties {
    public static String SEND_SMS_URL = "https://sdk2.028lk.com/sdk2/BatchSend2.aspx";

    public static String QUERY_SMS_URL = "https://sdk2.028lk.com/sdk2/SelSum.aspx?CorpID={CorpID}&Pwd={Pwd}";

    @Component
    public static class Sms {
        public static String SMS_KEY; //= "LC011165";

        public static String SMS_PWD; //= "sd@123456";

        @Value("${inspur.sms.key}")
        public void setSmsKey(String key) {
            SMS_KEY = key;
        }

        @Value("${inspur.sms.pwd}")
        public void setSmsPwd(String pwd) {
            SMS_PWD = pwd;
        }
    }
}