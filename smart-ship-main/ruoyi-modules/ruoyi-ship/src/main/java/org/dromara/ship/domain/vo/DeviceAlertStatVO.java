package org.dromara.ship.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author lyn
 */
@Schema(description = "设备告警统计")
@Data
public class DeviceAlertStatVO {

    @Schema(title = "设备id")
    private String deviceId;

    @Schema(title = "时间")
    private Date date;

    @Schema(title = "告警数量")
    private long num;
}
