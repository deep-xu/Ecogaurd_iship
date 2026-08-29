package org.dromara.ship.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.sql.Timestamp;

/**
 * 【请填写功能名称】对象 alert
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert")
public class Alert extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private String id;

    /**
     * 告警时间
     */
    private Long alertTime;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 描述
     */
    private String deviceDesc;

    /**
     * 数据源id
     */
    private String sourceId;

    /**
     * 数据源IP
     */
    private String sourceIpv4;

    /**
     * 数据源描述
     */
    private String sourceDesc;

    /**
     * 算法名称
     */
    private String algName;

    /**
     * 算法通道名称
     */
    private String algChName;

    /**
     * 危险等级
     */
    private String hazardLevel;

    /**
     * 图片路径
     */
    private String image;

    /**
     * 存储数据
     */
    private String reservedData;

    /**
     * 视频路径
     */
    private String video;

    /**
     * 状态
     */
    private Long status;


    /**
     * 部门ID
     */
    private Long deptId;

}
