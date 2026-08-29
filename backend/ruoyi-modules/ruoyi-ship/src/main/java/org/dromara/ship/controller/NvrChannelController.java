package org.dromara.ship.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.web.core.BaseController;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.bo.NvrChannelBo;
import org.dromara.ship.domain.bo.NvrChannelJson;
import org.dromara.ship.domain.vo.ChannelTreeVo;
import org.dromara.ship.domain.vo.NvrChannelVo;
import org.dromara.ship.domain.vo.NvrInfoVo;
import org.dromara.ship.service.INvrChannelService;
import org.dromara.ship.service.INvrInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * NVR通道信息
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ship/channel")
@Slf4j
public class NvrChannelController extends BaseController {

    private final INvrChannelService nvrChannelService;
    private final INvrInfoService invrInfoService;

    /**
     * 查询NVR通道信息列表
     */
    @SaCheckPermission("ship:channel:list")
    @GetMapping("/list")
    public TableDataInfo<NvrChannelVo> list(NvrChannelBo bo, PageQuery pageQuery) {
        return nvrChannelService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取NVR通道信息详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("ship:channel:query")
    @GetMapping("/{id}")
    public R<NvrChannelVo> getInfo(@NotNull(message = "主键不能为空")
                                   @PathVariable Long id) {
        return R.ok(nvrChannelService.queryById(id));
    }

    /**
     * 新增NVR通道信息
     */
    @SaCheckPermission("ship:channel:add")
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody NvrChannelBo bo) {
        return toAjax(nvrChannelService.insertByBo(bo));
    }

    /**
     * 修改NVR通道信息
     */
    @SaCheckPermission("ship:channel:edit")
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody NvrChannelBo bo) {
        return toAjax(nvrChannelService.updateByBo(bo));
    }

    /**
     * 删除NVR通道信息
     *
     * @param ids 主键串
     */
    @SaCheckPermission("ship:channel:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(nvrChannelService.deleteWithValidByIds(List.of(ids), true));
    }


    /**
     * 同步通道
     */
    @GetMapping("/syncChannel")
    public R<Void> syncChannel(@NotEmpty(message = "设备ID不能为空") String id) {
        NvrInfoVo vo = invrInfoService.queryById(id);
        if (Objects.isNull(vo)) {
            return R.fail("设备不存在");
        }
        String ip = RedisUtils.getCacheObject(CacheNames.NVR_ID_IP + vo.getId());
        if (StringUtils.isEmpty(ip)) {
            return R.fail("设备未连接");
        }
        List<NvrChannelJson> array = invrInfoService.getVoidList(id);
        if (CollUtil.isEmpty(array)) {
            return R.fail("设备获取失败");
        }
        log.warn("摄像头Json:{}", JsonUtils.toJsonString(array));
        Map<Integer, NvrChannelVo> groupMap = new HashMap<>();
        NvrChannelBo bo = new NvrChannelBo();
        bo.setNvrId(vo.getId());
        List<NvrChannelVo> voList = nvrChannelService.queryList(bo);
        if (CollUtil.isNotEmpty(voList)) {
            groupMap = StreamUtils.toIdentityMap(voList, NvrChannelVo::getChannelId);
        }
        nvrChannelService.deleteWithNvrId(vo.getId());
        List<NvrChannel> nvrChannels = new ArrayList<>();
        Map<Integer, NvrChannelVo> finalGroupMap = groupMap;
        array.forEach(item -> {
            NvrChannel nvrChannel = new NvrChannel();
            nvrChannel.setChannelId(item.getId());
            nvrChannel.setChannelName(item.getName());
            nvrChannel.setStatus(item.getStatus());
            nvrChannel.setNvrId(vo.getId());
            nvrChannel.setShipId(vo.getShipId());
            nvrChannel.setDeptId(vo.getDeptId());
            nvrChannel.setManufacturer(item.getManufacturer());
            nvrChannel.setAddress(item.getAddress());
            NvrChannelVo oldVo = finalGroupMap.get(item.getId());
            if (Objects.nonNull(oldVo)) {
                nvrChannel.setChannelGroup(oldVo.getChannelGroup());
                nvrChannel.setChannelName(oldVo.getChannelName());
            }
            nvrChannels.add(nvrChannel);
        });
        if (CollUtil.isNotEmpty(nvrChannels)) {
            nvrChannelService.batchSave(nvrChannels);
        }
        return R.ok("成功同步：" + nvrChannels.size() + "条");
    }

    /**
     * 根据分组获取设备信息
     */
    @GetMapping("/getChannelTree")
    public R<List<ChannelTreeVo>> getChannelTree() {
        return R.ok(nvrChannelService.getChannelTree());
    }
}
