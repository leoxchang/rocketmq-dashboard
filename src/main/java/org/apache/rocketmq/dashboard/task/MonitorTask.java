/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.dashboard.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.dashboard.model.ConsumerMonitorConfig;
import org.apache.rocketmq.dashboard.model.GroupConsumeInfo;
import org.apache.rocketmq.dashboard.model.MessagePage;
import org.apache.rocketmq.dashboard.model.MessageView;
import org.apache.rocketmq.dashboard.model.request.MessageQuery;
import org.apache.rocketmq.dashboard.model.request.TopicConfigInfo;
import org.apache.rocketmq.dashboard.service.*;
import org.apache.rocketmq.dashboard.sms.constant.AsrSmsCodes;
import org.apache.rocketmq.dashboard.sms.service.AsrSmsService;
import org.apache.rocketmq.dashboard.sms.service.AsrSmsUtil;
import org.apache.rocketmq.dashboard.sms.service.DingDingService;
import org.apache.rocketmq.dashboard.util.JsonUtil;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class MonitorTask {
    private Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    @Value("${rocketmq.monitor.topic}")
    private Integer topic;

    @Value("${rocketmq.monitor.phone}")
    private String phone;

    @Value("${rocketmq.monitor.DLQTopic.day}")
    private Integer dlqtopicDay;

    @Resource
    private MonitorService monitorService;

    @Resource
    private ConsumerService consumerService;

    @Resource
    private MQAdminExt mqAdminExt;

    @Resource
    private MessageService messageService;

    @Resource
    private TopicService topicService;

    @Resource
    private DingDingService dingDingService;

    @Resource
    private AsrSmsService asrSmsService;


    //    @Scheduled(cron = "* * * * * ?")
    public void scanProblemConsumeGroup() {
        for (Map.Entry<String, ConsumerMonitorConfig> configEntry : monitorService.queryConsumerMonitorConfig().entrySet()) {
            GroupConsumeInfo consumeInfo = consumerService.queryGroup(configEntry.getKey());
            if (consumeInfo.getCount() < configEntry.getValue().getMinCount() || consumeInfo.getDiffTotal() > configEntry.getValue().getMaxDiffTotal()) {
                logger.info("op=look consumeInfo {}", JsonUtil.obj2String(consumeInfo)); // notify the alert system
            }
        }
    }

    @Scheduled(cron = "${rocketmq.monitor.DLQTopic.cron}")
    public void scanDLQTopic() throws Exception {
        TopicList topicList = mqAdminExt.fetchAllTopicList();
        Set<String> topicSet = topicList.getTopicList();
        if (topicSet != null && topicSet.size() > 0) {
            Set<String> hasMessageTopicSet = new HashSet<String>();
            for (String topic : topicSet) {
                if (topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)) {
                    logger.info("DLQ_TOPIC是:" + topic);
                    List<TopicConfigInfo> topicConfigInfos = topicService.examineTopicConfig(topic);
                    if (topicConfigInfos != null && topicConfigInfos.size() > 0) {
                        TopicConfigInfo topicConfigInfo = topicConfigInfos.get(0);
                        if (topicConfigInfo.getPerm() != 6) {
                            topicConfigInfo.setPerm(6);
                            topicService.createOrUpdate(topicConfigInfo);
                        }
                        Date currDate = new Date();
                        Date threeDate = DateUtils.addDays(currDate, dlqtopicDay);
                        MessageQuery query = new MessageQuery();
                        query.setTopic(topic);
                        query.setTaskId("");
                        query.setBegin(threeDate.getTime());
                        query.setEnd(currDate.getTime());
                        logger.info("query:{}", JSON.toJSONString(query));
                        MessagePage messagePage = messageService.queryMessageByPage(query);
                        if (messagePage != null) {
                            Page<MessageView> views = messagePage.getPage();
                            if (views != null && views.getContent() != null && views.getContent().size() > 0) {
                                logger.info("死信topic:{}中有message,views.getContent size:{}", topic, views.getContent().size());
                                //发消息
                                hasMessageTopicSet.add(topic);
                            }
                        }
                    }
                }
            }
            if (hasMessageTopicSet.size() > 0) {
                String topicStr = String.join(",", hasMessageTopicSet);
                AsrSmsCodes codes = AsrSmsUtil.getAsrSmsCodes("dlq_Topic");
                dingDingService.sendToDingDing(codes, "mq-死信topic消息通知", topicStr);
                if (StringUtils.isNotBlank(phone)) {
                    if (phone.contains(",")) {//多个手机号
                        String[] phoneNos = phone.split(",");
                        for (String phoneNo : phoneNos) {
                            asrSmsService.sendSms(codes, phoneNo, topicStr);
                        }
                    } else {//单个手机号
                        asrSmsService.sendSms(codes, phone, topicStr);
                    }
                }
            }
        }
    }

    @Scheduled(cron = "${rocketmq.monitor.headUpGroup.cron}")
    public void scanNormalTopic() throws Exception {
        List<GroupConsumeInfo> groupConsumeInfos = consumerService.queryGroupList();
        if (groupConsumeInfos != null && groupConsumeInfos.size() > 0) {
            Set<String> MessageManyTopicSet = new HashSet<String>();
            for (GroupConsumeInfo groupConsumeInfo : groupConsumeInfos) {
                //发消息
                if (groupConsumeInfo.getDiffTotal() >= topic) {//message数量已经超过预警阈值
                    logger.info("consumer-group:{}中有消息堆积,Delay:{}", groupConsumeInfo.getGroup(), groupConsumeInfo.getDiffTotal());
                    MessageManyTopicSet.add(groupConsumeInfo.getGroup());
                }
            }
            if (MessageManyTopicSet.size() > 0) {
                String topicStr = String.join(",", MessageManyTopicSet);
                AsrSmsCodes codes = AsrSmsUtil.getAsrSmsCodes("head_up_Group");
                dingDingService.sendToDingDing(codes, "mq-consumer-group中message消息堆积通知", topicStr);
                if (StringUtils.isNotBlank(phone)) {
                    if (phone.contains(",")) {//多个手机号
                        String[] phoneNos = phone.split(",");
                        for (String phoneNo : phoneNos) {
                            asrSmsService.sendSms(codes, phoneNo, topicStr);
                        }
                    } else {//单个手机号
                        asrSmsService.sendSms(codes, phone, topicStr);
                    }
                }
            }
        }
    }

}
