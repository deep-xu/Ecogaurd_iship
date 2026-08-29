package org.dromara.ship.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.PlatformConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.ship.domain.Device;
import org.dromara.ship.domain.bo.DeviceBo;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.ship.domain.vo.DeviceVo;
import org.dromara.ship.domain.vo.ScreenVo;
import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.service.IDeviceService;
import org.dromara.ship.service.IPlatformClient;
import org.dromara.ship.service.IShipService;
import org.dromara.ship.util.SignatureGenerator;
import org.dromara.ship.util.ThirdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备管理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ship/deivce")
public class DeviceController extends BaseController {

    private final IDeviceService deviceService;

    private final IPlatformClient platformClient;

    private final IShipService shipService;


    private final ThirdApi thirdApi;
    /**
     * 设备统计
     */
    @GetMapping("/count")
    public R<ScreenVo> count() {
        ScreenVo screenVo = new ScreenVo();

        screenVo.setDeviceOnlineTotal(deviceService.count(new QueryWrapper<Device>().eq("status", "1")));
        screenVo.setDeviceOfflineTotal(deviceService.count(new QueryWrapper<Device>().eq("status", "0")));
        screenVo.setSourceStat(deviceService.querySourceStat());
        screenVo.setDeviceAlertStatVOList(deviceService.queryDeviceAlertStat());
        return R.ok(screenVo);
    }

    /**
     * 分页查询设备
     * @return
     */

    @SaCheckPermission("ship:device:list")
    @GetMapping("/list")
    public TableDataInfo<DeviceVo> list(DeviceBo bo, PageQuery pageQuery) {
       return deviceService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询数据源
     */
    @SaCheckPermission("ship:device:source")
    @GetMapping("/source")
    public String source(String deviceId) {
        return JSONUtil.toJsonStr(deviceService.queryById(deviceId).getSource());
    }

    /**
     * 获取实时视频流
     * @return
     */
    @SaCheckPermission("device:source:stream")
    @GetMapping("/stream")
    public String getStream(String deviceId, String sourceId){
        DeviceVo deviceVo = deviceService.queryById(deviceId);
        ShipVo shipVo = shipService.queryById(deviceVo.getShipId());
        String ip = shipVo.getPlatform();
        String port1 = shipVo.getPort1();
        String port2 = shipVo.getPort2();
        String authorization = thirdApi.getToken(shipVo.getAk(), shipVo.getSk(), ip, port1);
        JSONObject jsonObject =  JSONUtil.parseObj(platformClient.getStream("Bearer " + authorization, deviceId, sourceId, ip, port2));
        if(jsonObject.getInt("error_code") == 0){
            String url = jsonObject.getStr("data");
            jsonObject.set("data",replaceIpAndPort(url, ip, shipVo.getPort3()));
        }else {
            jsonObject.set("data", jsonObject.getStr("message"));
        }
        return jsonObject.toStringPretty();

    }
    /**
     * 获取实时视频流状态
     * @return
     */
    @SaCheckPermission("source:stream:attr")
    @GetMapping("/stream/attr")
    public String getStreamAttr(String deviceId, String stream, int drawSize){
        DeviceVo deviceVo = deviceService.queryById(deviceId);
        ShipVo shipVo = shipService.queryById(deviceVo.getShipId());
        String ip = shipVo.getPlatform();
        String port1 = shipVo.getPort1();
        String port2 = shipVo.getPort2();
        String authorization = thirdApi.getToken(shipVo.getAk(), shipVo.getSk(), ip, port1);
        JSONObject jsonObject =  JSONUtil.parseObj(platformClient.getStreamAttr("Bearer " + authorization, deviceId, stream, drawSize, ip, port2));
        return jsonObject.toStringPretty();

    }

    /**
     * 检查数据源是否在线
     * @return
     */
    @SaCheckPermission("device:stream:check")
    @GetMapping("/stream/check")
    public R<Void> check(String deviceId , String stream){
        DeviceVo deviceVo = deviceService.queryById(deviceId);
        ShipVo shipVo = shipService.queryById(deviceVo.getShipId());
        String ip = shipVo.getPlatform();
        String port1 = shipVo.getPort1();
        String port2 = shipVo.getPort2();
        String authorization = thirdApi.getToken(shipVo.getAk(), shipVo.getSk(), ip, port1);
        JSONObject obj =  JSONUtil.parseObj(platformClient.getStreamAttr("Bearer " + authorization, deviceId, stream, 1280, ip, port2));
        if (obj.getInt("error_code")!= 0) {
            return R.fail("the stream is offline, "+obj.getStr("error_msg"));
        } else {
            return R.ok("the stream is online");
        }
    }

    /**
     * 设备绑定轮船
     * @return
     */

    @SaCheckPermission("ship:device:bind")
    @RepeatSubmit
    @PutMapping()
    public R<Void> bind(String deviceId, Long shipId){
        return toAjax(deviceService.bind(deviceId,shipId));

    }

    /**
     * 设备解绑轮船
     * @return
     */

    @SaCheckPermission("ship:device:unbind")
    @RepeatSubmit
    @PutMapping("unbind")
    public R<Void> unbind(String deviceId, Long shipId){
        return toAjax(deviceService.unbind(deviceId,shipId));

    }

    /**
     * 手工同步所有设备和数据源
     * @return
     */
    @GetMapping("/syncDevice")
    public R<Void> syncDevice() {
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
                log.error("Synchronizing device data sources failed！");
                R.fail("Synchronizing device data sources failed！");
                //throw new RuntimeException(e);
            }
        });
        return R.ok("Synchronization of the device data source is successful！");
    }


//    private String getToken(String ak, String sk, String ip, String port1) {
//        long timestamp = System.currentTimeMillis() / 1000;
//        String sign = SignatureGenerator.generateSignature(sk, ak, timestamp, PlatformConstants.NONCE);
//        JSONObject json  = JSONUtil.parseObj(platformClient.getToken(sign, ak, timestamp,PlatformConstants.NONCE, ip , port1));
//        return json.getStr("data");
//    }


    private static String replaceIpAndPort(String url,String platform,String port3) {
        // 用正则表达式替换IP部分和端口部分
        return url.replaceAll("(?<=://)([^:/]+)(:\\d+)?", platform + ":" + port3);
    }





}
