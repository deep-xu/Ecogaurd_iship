package org.dromara.ship.domain.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * Nvr设备通道Json
 *
 * @author: Mr.Yang
 * @create: 2025-10-24
 **/
@Data
public class NvrChannelJson implements Serializable {

    /**
     * 通道号
     */
    @JsonProperty("ID")
    private Integer id;

    /**
     * 通道名称
     */
    @JsonProperty("Name")
    private String name;

    /**
     * 状态
     */
    @JsonProperty("Status")
    private Integer status;

    /**
     * 厂商
     */
    @JsonProperty("Manufacturer")
    private String manufacturer;

    /**
     * 设备型号
     */
    @JsonProperty("DeviceModel")
    private String deviceModel;

    /**
     * 离线原因
     */
    @JsonProperty("OffReason")
    private Integer offReason;

    /**
     * IP地址
     */
    private String address;

}
