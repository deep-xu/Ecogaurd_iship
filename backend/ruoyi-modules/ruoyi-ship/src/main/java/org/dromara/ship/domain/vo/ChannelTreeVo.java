package org.dromara.ship.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 *
 * 通道信息
 *
 * @author: Mr.Yang
 * @create: 2025-10-02
 **/
@Data
public class ChannelTreeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组编码
     */
    private String id;

    /**
     * 分组名称
     */
    private String label;

    /**
     * 类型
     */
    private String type;

    /**
     * Nvr设备ID
     */
    private String nvrId;

    /**
     * 通道ID
     */
    private Integer channelId;

    /**
     * 通道状态
     */
    private Integer status;

    /**
     * 选中状态
     */
    private Boolean checked = Boolean.FALSE;

    /**
     * 下级设备
     */
    private List<ChannelTreeVo> children;

}
