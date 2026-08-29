package org.dromara.common.netty.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息的dto
 *
 * @author zendwang
 */
@Data
public class NettyMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的session key 列表
     */
    private List<String> channelIPs;

    /**
     * 消息体
     */
    private String message;
}
