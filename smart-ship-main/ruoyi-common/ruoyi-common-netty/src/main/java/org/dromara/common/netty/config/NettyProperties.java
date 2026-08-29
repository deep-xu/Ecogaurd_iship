package org.dromara.common.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty 配置项
 *
 * @author 杨斌
 */
@Data
@ConfigurationProperties("netty")
public class NettyProperties {

    /**
     * 开启状态
     */
    private Boolean enabled;

    /**
     * 端口号
     */
    private Integer port;


    /**
     * 通道数
     */
    private Integer bossThreads;

    /**
     * 线程数
     */
    private Integer workerThreads;

    /**
     * 认证秘钥
     */
    private String secret;
}
