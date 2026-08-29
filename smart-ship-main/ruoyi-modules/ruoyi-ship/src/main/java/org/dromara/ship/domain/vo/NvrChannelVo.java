package org.dromara.ship.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.ship.domain.NvrChannel;

import java.io.Serial;
import java.io.Serializable;


/**
 * NVR通道信息视图对象 nvr_channel
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@Data
@AutoMapper(target = NvrChannel.class)
public class NvrChannelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 船ID
     */
    private Long shipId;

    /**
     * 所属NVR
     */
    private String nvrId;

    /**
     * 通道ID
     */
    private Integer channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 厂商名称
     */
    private String manufacturer;

    /**
     * 设备地址
     */
    private String address;

    /**
     * 设备状态
     */
    private Integer status;

    /**
     * 所属分组
     */
    private String channelGroup;

    @Translation(type = TransConstant.DICT_TYPE_TO_LABEL, mapper = "channelGroup", other = "channel_group")
    private String channelGroupName;


}
