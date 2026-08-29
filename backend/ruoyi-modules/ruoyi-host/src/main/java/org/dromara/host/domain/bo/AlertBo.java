package org.dromara.host.domain.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.host.component.BigDecimalToLong;
import org.dromara.host.domain.Alert;

import java.math.BigDecimal;

/**
 * 【请填写功能名称】业务对象 alert
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
//@AllArgsConstructor
@AutoMapper(target = Alert.class, reverseConvertGenerate = false, uses = BigDecimalToLong.class)

public class AlertBo extends BaseEntity {

    /**
     *{"msg_id": "67866083c380623ff98928e2", "msg_type": "alert", "time": "2025-01-14 21:02:59.555121",
     * "data": {"id": "678660835dc58a0a85a1b791", "alert_time": 1736859779.055076, "device_id": "d70ca96e-64ed-44fe-8449-0471ce337427",
     * "device_name": "shenzhen", "device_desc": null, "source_id": "670b167b5dc58a0954dac0e1", "source_ipv4": "192.168.2.64",
     * "source_desc": "shenzhen_1", "alg_name": "on_duty", "alg_ch_name": "On-Duty Detection", "hazard_level": "",
     * "image": "/opt/ks/proxy/data/alert/2025_01_14/670b167b5dc58a0954dac0e1/on_duty/21_02_59_055076/alert.jpg",
     * "reserved_data": "{}", "video": null, "status": 1}}
     */

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String id;

    /**
     * 告警时间
     */
    @NotNull(message = "告警时间不能为空", groups = { AddGroup.class, EditGroup.class })
    //@AutoMapping(target = "alertTime")
    private Long alertTime;


    /**
     * 设备id
     */

    @NotBlank(message = "设备id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deviceId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deviceName;

    /**
     * 描述
     */
    @NotBlank(message = "描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deviceDesc;

    /**
     * 数据源id
     */
    @NotBlank(message = "数据源id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sourceId;

    /**
     * 数据源IP
     */
    @NotBlank(message = "数据源IP不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sourceIpv4;

    /**
     * 数据源描述
     */
    @NotBlank(message = "数据源描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sourceDesc;

    /**
     * 算法名称
     */
    @NotBlank(message = "算法名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String algName;

    /**
     * 算法通道名称
     */
    @NotBlank(message = "算法通道名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String algChName;

    /**
     * 危险等级
     */
    @NotBlank(message = "危险等级不能为空", groups = { AddGroup.class, EditGroup.class })
    private String hazardLevel;

    /**
     * 图片路径
     */
    @NotBlank(message = "图片路径不能为空", groups = { AddGroup.class, EditGroup.class })
    private String image;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String reservedData;

    /**
     * 视频路径
     */
    @NotBlank(message = "视频路径不能为空", groups = { AddGroup.class, EditGroup.class })
    private String video;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;


    /**
     * 同步状态
     */
    @NotNull(message = "同步状态",  groups = { AddGroup.class, EditGroup.class })
    private Integer syncStatus;

    /**
     * 同步失败次数
     */
    @NotNull(message = "同步失败次数",  groups = { AddGroup.class, EditGroup.class })
    private Integer retryCount;

    /**
     * 最后同步时间
     */
    @NotNull(message = "最后同步时间",  groups = { AddGroup.class, EditGroup.class })
    private Long lastTryTime;
}
