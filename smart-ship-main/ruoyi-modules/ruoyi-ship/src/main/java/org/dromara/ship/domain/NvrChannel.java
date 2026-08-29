package org.dromara.ship.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * NVR通道信息对象 nvr_channel
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nvr_channel")
public class NvrChannel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
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


}
