package org.apache.rocketmq.dashboard.sms.constant;

public enum AsrSmsCodes {

    DLQ_TOPIC("dlq_Topic", "SMS_10008", "死信topic为：${topicName}，请注意查看！"),
    HEAD_UP_GROUP("head_up_Group", "SMS_10009", "Delay数量已经超过预警阈值，consumer-group为：${groupName}，请注意查看！"),
    UNKNOWN("unknown", "SMS_ERROR", "UNKNOWN");

    private String type;

    private String smsCode;

    private String smsContent;

    AsrSmsCodes(String type, String smsCode, String smsContent) {
        this.type = type;
        this.smsCode = smsCode;
        this.smsContent = smsContent;
    }

    public String getType() {
        return this.type;
    }

    public String getSmsCode() {
        return this.smsCode;
    }

    public String getSmsContent() {
        return this.smsContent;
    }
}
