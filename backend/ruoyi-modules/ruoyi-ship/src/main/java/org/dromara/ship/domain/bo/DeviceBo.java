package org.dromara.ship.domain.bo;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import io.github.linpeilie.annotations.AutoMapping;
import org.dromara.ship.domain.Device;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 【请填写功能名称】业务对象 device
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Device.class, reverseConvertGenerate = false)
public class DeviceBo extends BaseEntity {

    /**
     *{
     *             "id": "d70ca96e-64ed-44fe-8449-0471ce337427",
     *             "name": "shenzhen",
     *             "desc": null,
     *             "ipv4": "192.168.6.65",
     *             "infer_freq": 10.0,
     *             "max_source": 8,
     *             "hardware_version": "ks968-m2",
     *             "software_version": "3.1.2",
     *             "status": 1
     *         }
     */
    @NotBlank(message = "不能为空", groups = { EditGroup.class })
    private String id;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 描述
     */
    @AutoMapping(target = "description")
    private String desc;

    /**
     * ip地址
     */
    @NotBlank(message = "ip地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String ipv4;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long inferFreq;

    /**
     * 最大数据源
     */
    @NotNull(message = "最大数据源不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long maxSource;

    /**
     * 硬件版本
     */
    @NotBlank(message = "硬件版本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String hardwareVersion;

    /**
     * 软件版本
     */
    @NotBlank(message = "软件版本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String softwareVersion;

    /**
     * 在线状态
     */
    @NotBlank(message = "在线状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 船ID
     */
    @NotNull(message = "船ID不能为空", groups = { EditGroup.class })
    private Long shipId;


    private JSONObject source;

}
