package org.dromara.common.netty.listener;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.netty.core.NettyEmitterManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * SSE 主题订阅监听器
 *
 * @author Lion Li
 */
@Slf4j
public class NettyTopicListener implements ApplicationRunner, Ordered {


    /**
     * 在Spring Boot应用程序启动时初始化SSE主题订阅监听器
     *
     * @param args 应用程序参数
     */
    @Override
    public void run(ApplicationArguments args) {
        NettyEmitterManager.subscribeMessage((message) -> {
            log.info("SSE主题订阅收到消息session keys={} message={}", message.getChannelIPs(), message.getMessage());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getChannelIPs())) {
                message.getChannelIPs().forEach(key -> NettyEmitterManager.sendMessage(key, message.getMessage()));
            } else {
                NettyEmitterManager.sendMessage(message.getMessage());
            }
        });
        log.info("初始化Netty主题订阅监听器成功");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
