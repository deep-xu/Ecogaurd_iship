package org.dromara.ship.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.netty.WSBean.WebsocketCodeEnum;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;
import org.dromara.common.netty.core.NettyEmitterManager;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.web.core.BaseController;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.bo.NvrChannelJson;
import org.dromara.ship.domain.bo.NvrInfoBo;
import org.dromara.ship.domain.bo.NvrVideoBo;
import org.dromara.ship.domain.vo.NvrChannelVo;
import org.dromara.ship.domain.vo.NvrInfoVo;
import org.dromara.ship.mapper.NvrChannelMapper;
import org.dromara.ship.service.INvrInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nvr设备
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ship/nvr")
@Slf4j
public class NvrInfoController extends BaseController {

    private final INvrInfoService nvrService;
    private final NvrChannelMapper nvrChannelMapper;

    @Value("${srs.address}")
    private String srsAddress;

    /**
     * 分页查询Nvr设备
     */
    @SaCheckPermission("ship:nvr:list")
    @GetMapping("/list")
    public TableDataInfo<NvrInfoVo> list(NvrInfoBo bo, PageQuery pageQuery) {
        return nvrService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【Nvr设备】列表
     */
    @SaCheckPermission("ship:nvr:export")
    @Log(title = "【导出Nvr设备】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(NvrInfoBo bo, HttpServletResponse response) {
        List<NvrInfoVo> list = nvrService.queryList(bo);
        ExcelUtil.exportExcel(list, "【导出Nvr设备】", NvrInfoVo.class, response);
    }

    /**
     * 获取【通过ID查询轮船】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("ship:nvr:query")
    @GetMapping("/{id}")
    public R<NvrInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                @PathVariable String id) {
        return R.ok(nvrService.queryById(id));
    }

    /**
     * 新增【Nvr设备】
     */
    @SaCheckPermission("ship:nvr:add")
    @Log(title = "【新增Nvr设备】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody NvrInfoBo bo) {
        return toAjax(nvrService.insertByBo(bo));
    }

    /**
     * 修改【Nvr设备】
     */
    @SaCheckPermission("ship:nvr:edit")
    @Log(title = "【修改Nvr设备", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody NvrInfoBo bo) {
        return toAjax(nvrService.updateByBo(bo));
    }

    /**
     * 删除【Nvr设备】
     *
     * @param ids 主键串
     */
    @SaCheckPermission("ship:nvr:remove")
    @Log(title = "【删除【Nvr设备】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] ids) {
        return toAjax(nvrService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 查看设备的所有通道
     */
    @GetMapping("/getVoidList")
    public R<List<NvrChannelVo>> getVoidList(@NotEmpty(message = "设备ID不能为空") String id) {
        NvrInfoVo vo = nvrService.queryById(id);
        if (Objects.isNull(vo)) {
            return R.fail("设备不存在");
        }
        LambdaQueryWrapper<NvrChannel> lqw = Wrappers.lambdaQuery(NvrChannel.class);
        lqw.eq(NvrChannel::getNvrId, vo.getId());
        List<NvrChannelVo> channelVoList = nvrChannelMapper.selectVoList(lqw);
        List<NvrChannelJson> jsonList = nvrService.getVoidList(id);
        Map<Integer, Integer> statusMap = jsonList.stream().collect(Collectors.toMap(NvrChannelJson::getId, NvrChannelJson::getStatus));
        channelVoList.forEach(channelVo -> channelVo.setStatus(statusMap.get(channelVo.getChannelId())));
        return R.ok(channelVoList);
    }

    /**
     * 获取直播地址
     */
    @PostMapping("/getLiveStream")
    public R<String> getLiveStream(@RequestBody NvrVideoBo bo) {
        NvrInfoVo vo = nvrService.queryById(bo.getNvrId());
        if (Objects.isNull(vo)) {
            return R.fail("设备不存在");
        }
        String ip = RedisUtils.getCacheObject(CacheNames.NVR_ID_IP + vo.getId());
        if (StringUtils.isEmpty(ip)) {
            return R.fail("设备未连接");
        }
        List<NvrChannelJson> jsonList = nvrService.getVoidList(bo.getNvrId());
        NvrChannelJson data = jsonList.stream().filter(e -> Objects.equals(bo.getChannelId(), e.getId())).findFirst().orElse(null);
        if (Objects.nonNull(data)) {
            if (!Objects.equals(data.getStatus(), 1)) {
                return R.fail("Equipment channel abnormality！");
            }
            if (Objects.equals(bo.getType(), 0) && (CollUtil.isEmpty(bo.getTimeRange()))) {
                return R.fail("Please select the Time");
            }
            if (Objects.equals(data.getStatus(), 1)) {
                List<Dict> sessionList = getChangeSession(ip, String.valueOf(bo.getChannelId()), bo.getType());
                if (CollUtil.isEmpty(sessionList)) {
                    String playAddress = createLiveUrl(bo, ip);
                    return R.ok(null, playAddress);
                } else {
                    Dict fast = sessionList.get(0);
                    String playAddress;
                    String sessionId = fast.getStr("SessionID");
                    deleteLiveSession(bo, ip, sessionId);
                    playAddress = createLiveUrl(bo, ip);
                    return R.ok(null, playAddress);
                }
            }
        }
        return R.fail("error");
    }

    /**
     * 移除会话
     */
    private void deleteLiveSession(NvrVideoBo bo, String ip, String sessionId) {
        WebsocketReq websocketReq = new WebsocketReq();
        websocketReq.setRequestURL("/LAPI/V1.0/Channels/" + bo.getChannelId() + "/Media/Video/Streams/" + 1 + "/Sessions/" + sessionId);
        websocketReq.setMethod("DELETE");
        NettyEmitterManager.sendMessage(ip, websocketReq);
    }

    /**
     * 获取播放地址
     *
     */
    private String createLiveUrl(NvrVideoBo bo, String ip) {
        WebsocketReq websocketReq = new WebsocketReq();
        int streamID = 0;
        if (bo.getType() == 0) {
            streamID = 1;
        }
        websocketReq.setRequestURL("/LAPI/V1.0/Channels/" + bo.getChannelId() + "/Media/Video/Streams/" + streamID + "/Sessions");
        websocketReq.setMethod("POST");
        Map<String, Object> params = new LinkedHashMap<>();
        String sessionId = IdUtil.simpleUUID();
        String address = "rtmp://" + srsAddress + "/live/" + sessionId;
        log.info("推流地址：{}", address);
        params.put("SessionID", sessionId);
        params.put("Type", bo.getType());
        Map<String, Object> dest = new LinkedHashMap<>();
        dest.put("AddressType", 2);
        dest.put("Address", address);
        params.put("Dest", dest);
        params.put("TransMode", 13);
        if (Objects.equals(bo.getType(), 0)) {
            LocalDateTime begin = LocalDateTimeUtil.parse(bo.getTimeRange().get(0), "yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime = begin.atZone(ZoneId.of("UTC"));
            params.put("Begin", zonedDateTime.toInstant().getEpochSecond());
            LocalDateTime end = LocalDateTimeUtil.parse(bo.getTimeRange().get(1), "yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime1 = end.atZone(ZoneId.of("UTC"));
            params.put("End", zonedDateTime1.toInstant().getEpochSecond());
        }
        websocketReq.setData(params);
        WebsocketRsp rsp = NettyEmitterManager.sendMessage(ip, websocketReq);
        log.error("拉流返回：{}", rsp);
        if (Objects.nonNull(rsp) && Objects.equals(rsp.getResponseCode(), WebsocketCodeEnum.SUCCESS.getCode())) {
            return "https://ishipplus.com:8443/srs-live/live/" + sessionId + ".flv";
        }
        return "";
    }

    /**
     * 获取通道视频流
     *
     * @param ip        Nvr设备
     * @param channelId 通道ID
     */
    private List<Dict> getChangeSession(String ip, String channelId, Integer type) {
        WebsocketReq websocketReq = new WebsocketReq();
        websocketReq.setRequestURL("/LAPI/V1.0/Channels/" + channelId + "/Media/Video/Streams/Sessions");
        websocketReq.setMethod("GET");
        WebsocketRsp rsp = NettyEmitterManager.sendMessage(ip, websocketReq);
        List<Dict> sessionInfoList = new ArrayList<>();
        if (rsp != null && Objects.equals(rsp.getResponseCode(), WebsocketCodeEnum.SUCCESS.getCode())) {
            Dict data = JsonUtils.parseMap(JsonUtils.toJsonString(rsp.getData()));
            if (Objects.nonNull(data)) {
                List<Dict> sessionList = JsonUtils.parseArrayMap(JsonUtils.toJsonString(data.get("SessionInfoList")));
                if (sessionList != null) {
                    for (Dict session : sessionList) {
                        if (Objects.equals(session.get("Type"), type)) {
                            sessionInfoList.add(session);
                        }
                    }
                }
            }
        }
        return sessionInfoList;
    }

    /**
     * 检查是否存在录像
     */
    @GetMapping("/checkPlayVideo")
    public R<Integer> checkPlayVideo(NvrVideoBo bo) {
        NvrInfoVo vo = nvrService.queryById(bo.getNvrId());
        if (Objects.isNull(vo)) {
            return R.fail("设备不存在");
        }
        String ip = RedisUtils.getCacheObject(CacheNames.NVR_ID_IP + vo.getId());
        if (StringUtils.isEmpty(ip)) {
            return R.fail("设备未连接");
        }
        LocalDateTime begin = LocalDateTimeUtil.parse(bo.getTimeRange().get(0), "yyyy-MM-dd HH:mm:ss");
        ZonedDateTime startTime = begin.atZone(ZoneId.of("UTC"));
        LocalDateTime end = LocalDateTimeUtil.parse(bo.getTimeRange().get(1), "yyyy-MM-dd HH:mm:ss");
        ZonedDateTime endTime = end.atZone(ZoneId.of("UTC"));
        String url = "/LAPI/V1.0/Channels/" + bo.getChannelId() + "/Media/Video/Streams/" + 1 + "/Records?" + "Begin=" + startTime.toInstant().getEpochSecond() +
            "&End=" + endTime.toInstant().getEpochSecond();
        WebsocketReq websocketReq = new WebsocketReq();
        websocketReq.setRequestURL(url);
        websocketReq.setMethod("GET");
        WebsocketRsp rsp = NettyEmitterManager.sendMessage(ip, websocketReq);
        log.error("录像查询结果：{}", rsp);
        int num = 0;
        if (Objects.nonNull(rsp) && Objects.equals(rsp.getResponseCode(), WebsocketCodeEnum.SUCCESS.getCode())) {
            Dict data = JsonUtils.parseMap(JsonUtils.toJsonString(rsp.getData()));
            if (Objects.nonNull(data)) {
                num = data.getInt("Nums");
            }
        }
        return R.ok(num);
    }

}
