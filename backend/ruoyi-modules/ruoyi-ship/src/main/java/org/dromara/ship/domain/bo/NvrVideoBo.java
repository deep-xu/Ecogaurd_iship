package org.dromara.ship.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 *
 * Nvr视频流请求
 *
 * @author: Mr.Yang
 * @create: 2025-10-10
 **/
@Data
public class NvrVideoBo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * nvr设备ID
     */
    private String nvrId;

    /**
     * 通道ID
     */
    private Integer channelId;

    /**
     * 播放类型 0回放 1实况
     */
    private Integer type;

    /**
     * 回放开始时间
     */
    private List<String> timeRange;

}
