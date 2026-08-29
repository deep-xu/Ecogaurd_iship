package org.dromara.common.netty.core;

import cn.hutool.core.collection.CollUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;
import org.dromara.common.netty.config.WSOperator;
import org.dromara.common.netty.domain.NettyMessageDto;
import org.dromara.common.redis.utils.RedisUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 管理 Server-Sent Events (NETTY) 连接
 *
 * @author Lion Li
 */
@Slf4j
public class NettyEmitterManager {

    /**
     * 订阅的频道
     */
    private final static String NETTY_TOPIC = "global:netty";

    public static final ConcurrentHashMap<String, ChannelHandlerContext> NETTY_DEVICE_CHANNELS = new ConcurrentHashMap<>();

    /**
     * 将Netty通道添加到会话Map中
     */
    public static void addChannel(String channelIP, ChannelHandlerContext channel) {
        removeChannel(channelIP);
        NETTY_DEVICE_CHANNELS.put(channelIP, channel);
    }

    /**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     *
     * @param channelIP 要移除的会话键
     */
    public static void removeChannel(String channelIP) {
        ChannelHandlerContext session = NETTY_DEVICE_CHANNELS.remove(channelIP);
        if (session != null) {
            session.close();
        }
    }


    /**
     * 订阅NETTY消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理NETTY消息的消费者函数
     */
    public static void subscribeMessage(Consumer<NettyMessageDto> consumer) {
        RedisUtils.subscribe(NETTY_TOPIC, NettyMessageDto.class, consumer);
    }


    /**
     * 向指定的用户会话发送消息
     *
     * @param channelIP 设备编码
     * @param message   要发送的消息内容
     */
    public static void sendMessage(String channelIP, String message) {
        ChannelHandlerContext chx = NETTY_DEVICE_CHANNELS.get(channelIP);
        if (chx != null) {
            chx.channel().writeAndFlush(message);
        } else {
            NETTY_DEVICE_CHANNELS.remove(channelIP);
        }
    }

    /**
     * 带返回值消息
     *
     * @param channelIP    通道IP
     * @param websocketReq 请求参数
     * @return 响应结果
     */
    public static WebsocketRsp sendMessage(String channelIP, WebsocketReq websocketReq) {
        ChannelHandlerContext chx = NETTY_DEVICE_CHANNELS.get(channelIP);
        if (chx != null) {
            return WSOperator.sendLapiRequest(chx, websocketReq.getRequestURL(), websocketReq.getMethod(), websocketReq.getData());
        } else {
            NETTY_DEVICE_CHANNELS.remove(channelIP);
            return null;
        }
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public static void sendMessage(String message) {
        for (String channelIP : NETTY_DEVICE_CHANNELS.keySet()) {
            sendMessage(channelIP, message);
        }
    }

    /**
     * 发布NETTY订阅消息
     *
     * @param nettyMessageDto 要发布的NETTY消息对象
     */
    public static void publishMessage(NettyMessageDto nettyMessageDto) {
        List<String> unsentSessionKeys = new ArrayList<>();
        for (String deviceId : nettyMessageDto.getChannelIPs()) {
            if (NETTY_DEVICE_CHANNELS.containsKey(deviceId)) {
                NettyEmitterManager.sendMessage(deviceId, nettyMessageDto.getMessage());
                continue;
            }
            unsentSessionKeys.add(deviceId);
        }
        if (CollUtil.isNotEmpty(unsentSessionKeys)) {
            NettyMessageDto broadcastMessage = new NettyMessageDto();
            broadcastMessage.setChannelIPs(unsentSessionKeys);
            broadcastMessage.setMessage(nettyMessageDto.getMessage());
            RedisUtils.publish(NETTY_TOPIC, broadcastMessage, consumer -> log.info("NETTY发送主题订阅消息topic:{} session keys:{} message:{}",
                NETTY_TOPIC, nettyMessageDto.getChannelIPs(), nettyMessageDto.getMessage()));
        }
    }
}
