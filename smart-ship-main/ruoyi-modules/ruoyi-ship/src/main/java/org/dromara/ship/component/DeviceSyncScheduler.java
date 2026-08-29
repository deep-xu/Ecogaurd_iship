package org.dromara.ship.component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.ship.domain.bo.DeviceBo;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.ship.domain.vo.DeviceVo;
import org.dromara.ship.service.IDeviceService;
import org.dromara.ship.service.IPlatformClient;
import org.dromara.ship.service.IShipService;
import org.dromara.ship.util.ThirdApi;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author lyn
 */
@Slf4j
//@Component
@RequiredArgsConstructor
public class DeviceSyncScheduler {

    private final IDeviceService deviceService;

    private final IShipService shipService;

    private final IPlatformClient platformClient;

    private final ThirdApi thirdApi;

    // 每隔 30 分钟尝试同步一次
    @Scheduled(fixedRate = 1800000)
    public void scheduleSync() {
        log.info("开始同步设备数据");
        shipService.queryList(new ShipBo()).forEach(shipVo -> {
            try {
                String ip = shipVo.getPlatform();
                String port1 = shipVo.getPort1();
                String ak = shipVo.getAk();
                String sk = shipVo.getSk();
                String authorization = thirdApi.getToken(ak, sk, ip, port1);
                //初始化设备数据
                JSONObject obj = JSONUtil.parseObj(platformClient.getDevice("Bearer " + authorization, ip, port1));
                if (obj.getInt("error_code") == 0) {
                    List<DeviceBo> deviceBoList = JSONUtil.toList(obj.getJSONArray("data"), DeviceBo.class);
                    deviceBoList.forEach(deviceBo -> {
                        DeviceVo deviceVo = deviceService.queryById(deviceBo.getId());
                        if (deviceVo == null) {
                            deviceService.insertByBo(deviceBo);
                        } else {
                            deviceService.updateByBo(deviceBo);
                        }
                        //初始化数据源数据
                        JSONObject sourceObj = JSONUtil.parseObj(platformClient.getSource("Bearer " + authorization, deviceBo.getId(), ip, port1));
                        if (sourceObj.getInt("error_code") == 0) {
                            String sourceJson = sourceObj.getJSONObject("data").getStr(deviceBo.getId());
                            deviceBo.setSource(JSONUtil.parseObj(sourceJson));
                            deviceService.updateByBo(deviceBo);
                        }
                    });
                }
            } catch (RuntimeException e) {
                log.error("同步设备数据时发生错误", e);
                //throw new RuntimeException(e);
            }
        });
        log.info("完成同步设备数据");
    }
}
