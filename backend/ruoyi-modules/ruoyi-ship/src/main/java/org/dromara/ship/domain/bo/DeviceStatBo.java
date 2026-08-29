package org.dromara.ship.domain.bo;


import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ship.domain.Device;

import java.math.BigDecimal;

/**
 * @author lyn
 */
@Data
@AutoMapper(target = Device.class, reverseConvertGenerate = false)
public class DeviceStatBo {

   /* private String msgId;

    private String msgType;

    private BigDecimal time;

    private DeviceData data;*/


   // public static class DeviceData {

    private String deviceId;

    private Integer status;
   // }


}
