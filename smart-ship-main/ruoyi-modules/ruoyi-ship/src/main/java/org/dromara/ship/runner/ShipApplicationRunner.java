package org.dromara.ship.runner;

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
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 初始化设备业务数据
 *
 * @author lyn
 */

@Slf4j
@RequiredArgsConstructor
//@Component
public class ShipApplicationRunner //implements ApplicationRunner
 {

    private final IDeviceService deviceService;

    private final IShipService shipService;

    private final IPlatformClient platformClient;

    private final ThirdApi thirdApi;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public void run(org.springframework.boot.ApplicationArguments args) {

        scheduledExecutorService.scheduleWithFixedDelay(()->{
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
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }, 5, 60 * 10L, TimeUnit.SECONDS);
        log.info("初始化设备业务数据成功！");
    }


    /*private String getToken() {
        long timestamp = System.currentTimeMillis() / 1000;
        String sign = SignatureGenerator.generateSignature(PlatformConstants.SK, PlatformConstants.AK, timestamp, PlatformConstants.NONCE);
        JSONObject json  = JSONUtil.parseObj(platformClient.getToken(sign, PlatformConstants.AK,timestamp,PlatformConstants.NONCE));
        return json.getStr("data");
    }*/
}
