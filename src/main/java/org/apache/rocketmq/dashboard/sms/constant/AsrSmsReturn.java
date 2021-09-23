package org.apache.rocketmq.dashboard.sms.constant;

public enum AsrSmsReturn {
    OK("OK","OK"),
    NOT_REG("–1", "账号未注册"),
    OTHER_ERROR("–2", "其他错误"),
    USER_ERROR("–3", "帐号或密码错误"),
    NO_MONEY("–5", "余额不足，请充值"),
    TIMER_ERROR("–6", "定时发送时间不是有效的时间格式"),
    NO_SIGN("-7", "提交信息末尾未签名，请添加中文的企业签名【】或内容乱码"),
    CONTENT_TOO_LONG("–8", "发送内容需在1到300字之间"),
    PHONE_ERROR("-9", "发送号码为空"),
    TIME_OUT("-10", "定时时间不能小于系统当前时间"),
    PHONE_BLACK("-100", "IP黑名单"),
    USER_BLACK("-102", "账号黑名单"),
    IP_ERROR("-103", "IP未导白");

    private String errorCode;

    private String errorMsg;

    AsrSmsReturn(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}
