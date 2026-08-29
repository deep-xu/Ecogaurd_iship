package org.dromara.ship.domain;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 【请填写功能名称】对象 device
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "device", autoResultMap = true)
public class Device extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id")
    private String id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * ip地址
     */
    private String ipv4;

    /**
     *
     */
    private Long inferFreq;

    /**
     * 最大数据源
     */
    private Long maxSource;

    /**
     * 硬件版本
     */
    private String hardwareVersion;

    /**
     * 软件版本
     */
    private String softwareVersion;

    /**
     * 在线状态
     */
    private String status;

    /**
     * 船ID
     */
    private Long shipId;


    @TableField(value = "source", typeHandler = JacksonTypeHandler.class)
    private JSONObject source;
}
