package org.dromara.common.netty.WSBean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * 保活响应
 */
@Data
public class KeepAliveRspAO {
    /**
     * 服务器当前UTC时间
     */
    @JsonProperty("Timestamp")
    private Long timestamp;

    /**
     * 下次心跳间隔，单位：秒
     */
    @JsonProperty("Timeout")
    private Integer timeout;
}
