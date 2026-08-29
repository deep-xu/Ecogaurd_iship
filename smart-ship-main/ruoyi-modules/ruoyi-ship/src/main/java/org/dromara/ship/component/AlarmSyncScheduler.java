package org.dromara.ship.component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.ship.domain.bo.DeviceBo;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.ship.service.IAlertService;
import org.dromara.ship.service.IPlatformClient;
import org.dromara.ship.service.IShipService;
import org.dromara.ship.util.ThirdApi;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author lyn
 */
@Component
@RequiredArgsConstructor
public class AlarmSyncScheduler {


    private IAlertService alertService;


    private IShipService shipService;

    private IPlatformClient platformClient;

    private ThirdApi thirdApi;

    // 每隔 5 分钟尝试同步一次
    //@Scheduled(fixedRate = 300000)
    public void scheduleSync() {
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
                        //查询告警记录的最大时间
                        //long timestamp = alertService.getMaxTime(deviceBo.getId());
                        //long startTime = timestamp > 0 ? timestamp : System.currentTimeMillis()/1000 - 60 * 60 * 24;
                        long startTime = System.currentTimeMillis()/1000 - 60 * 60 * 24;
                        //初始化数据源数据
                        JSONObject sourceObj = JSONUtil.parseObj(platformClient.getAlert("Bearer " + authorization, ip, port1,1,100, startTime));
                        if (sourceObj.getInt("error_code") == 0) {
                            String sourceJson = sourceObj.getJSONObject("data").getStr(deviceBo.getId());
                            deviceBo.setSource(JSONUtil.parseObj(sourceJson));
                            //deviceService.updateByBo(deviceBo);
                        }
                    });
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
