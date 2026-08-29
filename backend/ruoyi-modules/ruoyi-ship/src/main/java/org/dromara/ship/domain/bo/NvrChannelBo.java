package org.dromara.ship.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.ship.domain.NvrChannel;

/**
 * NVR通道信息业务对象 nvr_channel
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = NvrChannel.class, reverseConvertGenerate = false)
public class NvrChannelBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
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
    private String channelId;

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
    private String status;

    /**
     * 所属分组
     */
    private String channelGroup;


}
