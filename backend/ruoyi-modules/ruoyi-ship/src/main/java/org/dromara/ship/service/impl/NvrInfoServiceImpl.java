package org.dromara.ship.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.service.NvrService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;
import org.dromara.common.netty.core.NettyEmitterManager;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.NvrInfo;
import org.dromara.ship.domain.bo.NvrChannelJson;
import org.dromara.ship.domain.bo.NvrInfoBo;
import org.dromara.ship.domain.vo.NvrInfoVo;
import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.mapper.NvrChannelMapper;
import org.dromara.ship.mapper.NvrInfoMapper;
import org.dromara.ship.mapper.ShipMapper;
import org.dromara.ship.service.INvrInfoService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 【Nvr设备】Service业务层处理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@RequiredArgsConstructor
@Service
public class NvrInfoServiceImpl implements INvrInfoService, NvrService {

    private final NvrInfoMapper baseMapper;
    private final ShipMapper shipMapper;
    private final NvrChannelMapper nvrChannelMapper;

    /**
     * 查询【通过ID查询Nvr设备】
     *
     * @param id 主键
     * @return 【Nvr设备】
     */
    @Override
    public NvrInfoVo queryById(String id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询【Nvr设备】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【Nvr设备】分页列表
     */
    @Override
    public TableDataInfo<NvrInfoVo> queryPageList(NvrInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<NvrInfo> lqw = buildQueryWrapper(bo);
        Page<NvrInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        if (CollUtil.isNotEmpty(result.getRecords())) {
            Set<Long> shipIds = StreamUtils.toSet(result.getRecords(), NvrInfoVo::getShipId);
            Map<Long, ShipVo> shipVoMap = new HashMap<>();
            if (!shipIds.isEmpty()) {
                List<ShipVo> shipVoList = shipMapper.selectVoByIds(shipIds);
                shipVoMap.putAll(StreamUtils.toIdentityMap(shipVoList, ShipVo::getId));
            }
            result.getRecords().forEach(item -> {
                String ip = RedisUtils.getCacheObject(CacheNames.NVR_ID_IP + item.getId());
                item.setIp(ip);
                ShipVo shipVo = shipVoMap.get(item.getShipId());
                if (shipVo != null) {
                    item.setShipName(shipVo.getName());
                }
            });
        }
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的【Nvr设备】列表
     *
     * @param bo 查询条件
     * @return 【Nvr设备】列表
     */
    @Override
    public List<NvrInfoVo> queryList(NvrInfoBo bo) {
        LambdaQueryWrapper<NvrInfo> lqw = buildQueryWrapper(bo);
        baseMapper.selectList();
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<NvrInfo> buildQueryWrapper(NvrInfoBo bo) {
        LambdaQueryWrapper<NvrInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getId()), NvrInfo::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), NvrInfo::getName, bo.getName());
        return lqw;
    }

    /**
     * 新增【Nvr设备】
     *
     * @param bo 【Nvr设备】
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(NvrInfoBo bo) {
        NvrInfo add = MapstructUtils.convert(bo, NvrInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【Nvr设备】
     *
     * @param bo 【Nvr设备】
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(NvrInfoBo bo) {
        NvrInfo update = MapstructUtils.convert(bo, NvrInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.insertOrUpdate(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(NvrInfo entity) {
        //TODO 做一些数据校验,如唯一约束
        if (entity.getShipId() != null) {
            ShipVo shipVo = shipMapper.selectVoById(entity.getShipId());
            entity.setDeptId(shipVo.getDeptId());
        } else {
            entity.setDeptId(LoginHelper.getDeptId());
        }
    }

    /**
     * 校验并批量删除【Nvr设备】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<NvrChannelJson> getVoidList(String nvrId) {
        List<NvrChannelJson> list = new ArrayList<>();
        String ip = RedisUtils.getCacheObject(CacheNames.NVR_ID_IP + nvrId);
        if (StringUtils.isNotBlank(ip)) {
            WebsocketReq websocketReq = new WebsocketReq();
            websocketReq.setRequestURL("/LAPI/V1.0/Channels/System/ChannelDetailInfos");
            websocketReq.setMethod("GET");
            WebsocketRsp rsp = NettyEmitterManager.sendMessage(ip, websocketReq);
            if (Objects.nonNull(rsp) && Objects.nonNull(rsp.getData())) {
                Dict dict = JsonUtils.parseMap(JsonUtils.toJsonString(rsp.getData()));
                if (dict != null) {
                    List<Dict> array = JsonUtils.parseArrayMap(JsonUtils.toJsonString(dict.get("DetailInfos")));
                    if (array != null) {
                        array.forEach(item -> {
                            NvrChannelJson json = JsonUtils.parseObject(JsonUtils.toJsonString(item), NvrChannelJson.class);
                            if (Objects.nonNull(json)) {
                                Dict addressInfo = JsonUtils.parseMap(JsonUtils.toJsonString(item.get("AddressInfo")));
                                if (Objects.nonNull(addressInfo)) {
                                    json.setAddress(addressInfo.getStr("Address"));
                                }
                                list.add(json);
                            }
                        });
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String checkNvrInfo(String nvrId) {
        LambdaQueryWrapper<NvrInfo> lqw = Wrappers.lambdaQuery();
        lqw.select(NvrInfo::getId, NvrInfo::getPassword);
        lqw.eq(NvrInfo::getId, nvrId);
        lqw.last("limit 1");
        NvrInfoVo infoVo = baseMapper.selectVoOne(lqw);
        if (infoVo != null) {
            return infoVo.getPassword();
        }
        return "";
    }

    @Override
    public void cleanGroup(String groupValue) {
        LambdaUpdateWrapper<NvrChannel> luw = Wrappers.lambdaUpdate();
        luw.eq(NvrChannel::getChannelGroup, groupValue);
        luw.set(NvrChannel::getChannelGroup, null);
        nvrChannelMapper.update(luw);
    }
}
