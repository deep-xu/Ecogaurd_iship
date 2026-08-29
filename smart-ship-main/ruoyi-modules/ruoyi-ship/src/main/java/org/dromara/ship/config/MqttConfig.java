package org.dromara.ship.config;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.dromara.ship.domain.Device;
import org.dromara.ship.domain.bo.AlertBo;
import org.dromara.ship.domain.bo.DeviceStatBo;
import org.dromara.ship.domain.vo.AlertVo;
import org.dromara.ship.domain.vo.DeviceVo;
import org.dromara.ship.service.IAlertService;
import org.dromara.ship.service.IDeviceService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lyn
 */
@Configuration
@IntegrationComponentScan
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Resource
    private IAlertService alertService;

    @Resource
    private IDeviceService deviceService;
    @Bean
    public MqttPahoClientFactory mqttPahoClientFactory(){
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options  = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel(){
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound(){
        List<String> topicList = Arrays.asList(topic.trim().split(","));
        String[] topics = new String[topicList.size()];
        topicList.toArray(topics);
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, mqttPahoClientFactory(), topics);
        adapter.setCompletionTimeout(5000);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    //@Bean
    //@ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler(){
        return message -> {
            String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
            String payload = message.getPayload().toString();
            JSONObject jsonObject = JSONUtil.parseObj(payload);
            JSONObject data = jsonObject.getJSONObject("data");
            if("ks/proxy/message".equals(topic)){
                String msgType = jsonObject.getStr("msg_type");
                switch (msgType){
                    case "device_status":
                        DeviceStatBo deviceStatBo = JSONUtil.toBean(data, DeviceStatBo.class);
                        deviceService.update(new LambdaUpdateWrapper<Device>().set(Device::getStatus,deviceStatBo.getStatus()).eq(Device::getId,deviceStatBo.getDeviceId()));
                        break;
                    case "source":
                        data.forEach((key, value) -> {
                            JSONObject jsonObject1 = JSONUtil.parseObj(value);
                            deviceService.update(new LambdaUpdateWrapper<Device>().set(Device::getSource,jsonObject1).eq(Device::getId,key));
                        });
                        break;
                    /*case "network":
                        break;    */
                    default:
                        break;
                }
            }else if(topic.startsWith("ks/proxy/alert/")){
                AlertBo alertBo = JSONUtil.toBean(data, AlertBo.class);
                alertService.insertByBo(alertBo);
            }
        };
    }

    @Bean
    public MessageChannel mqttOutboundChannel(){
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(){
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("clientOutBound", mqttPahoClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("ks/proxy/alert/");
        return  messageHandler;
    }
}
