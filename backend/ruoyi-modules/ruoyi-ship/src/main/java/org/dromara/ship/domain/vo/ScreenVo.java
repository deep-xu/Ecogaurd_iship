package org.dromara.ship.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author lyn
 */
@Schema(description = "设备统计信息")
@Data
public class ScreenVo {


    //private long deviceTotal;

    @Schema(title = "设备在线数")
    private long deviceOnlineTotal;

    @Schema(title = "设备离线数")
    private long deviceOfflineTotal;

    @Schema(title = "设备告警统计")
    private List<DeviceAlertStatVO> deviceAlertStatVOList;

    @Schema(title = "数据源在线离线统计")
    private Map<String, Integer> sourceStat;


}
