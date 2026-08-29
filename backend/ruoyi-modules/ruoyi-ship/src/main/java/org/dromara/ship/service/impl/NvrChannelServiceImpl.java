package org.dromara.ship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.bo.NvrChannelBo;
import org.dromara.ship.domain.bo.NvrChannelJson;
import org.dromara.ship.domain.vo.ChannelTreeVo;
import org.dromara.ship.domain.vo.NvrChannelVo;
import org.dromara.ship.mapper.NvrChannelMapper;
import org.dromara.ship.mapper.ShipMapper;
import org.dromara.ship.service.INvrChannelService;
import org.dromara.ship.service.INvrInfoService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * NVR通道信息Service业务层处理
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NvrChannelServiceImpl implements INvrChannelService {

    private final NvrChannelMapper baseMapper;
    private final DictService dictService;
    private final ShipMapper shipMapper;
    private final INvrInfoService nvrService;


    /**
     * 查询NVR通道信息
     *
     * @param id 主键
     * @return NVR通道信息
     */
    @Override
    public NvrChannelVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询NVR通道信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return NVR通道信息分页列表
     */
    @Override
    public TableDataInfo<NvrChannelVo> queryPageList(NvrChannelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<NvrChannel> lqw = buildQueryWrapper(bo);
        Page<NvrChannelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的NVR通道信息列表
     *
     * @param bo 查询条件
     * @return NVR通道信息列表
     */
    @Override
    public List<NvrChannelVo> queryList(NvrChannelBo bo) {
        LambdaQueryWrapper<NvrChannel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<NvrChannel> buildQueryWrapper(NvrChannelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<NvrChannel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(NvrChannel::getId);
        lqw.eq(bo.getShipId() != null, NvrChannel::getShipId, bo.getShipId());
        lqw.eq(bo.getNvrId() != null, NvrChannel::getNvrId, bo.getNvrId());
        lqw.eq(StringUtils.isNotBlank(bo.getChannelId()), NvrChannel::getChannelId, bo.getChannelId());
        lqw.like(StringUtils.isNotBlank(bo.getChannelName()), NvrChannel::getChannelName, bo.getChannelName());
        lqw.like(StringUtils.isNotBlank(bo.getManufacturer()), NvrChannel::getManufacturer, bo.getManufacturer());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), NvrChannel::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getChannelGroup()), NvrChannel::getChannelGroup, bo.getChannelGroup());
        return lqw;
    }

    /**
     * 新增NVR通道信息
     *
     * @param bo NVR通道信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(NvrChannelBo bo) {
        NvrChannel add = MapstructUtils.convert(bo, NvrChannel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改NVR通道信息
     *
     * @param bo NVR通道信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(NvrChannelBo bo) {
        NvrChannel update = MapstructUtils.convert(bo, NvrChannel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(NvrChannel entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除NVR通道信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public void batchSave(Collection<NvrChannel> nvrChannels) {
        baseMapper.insertBatch(nvrChannels);
    }

    @Override
    public void deleteWithNvrId(String nvrId) {
        LambdaQueryWrapper<NvrChannel> lqw = Wrappers.lambdaQuery();
        lqw.eq(NvrChannel::getNvrId, nvrId);
        baseMapper.delete(lqw);
    }

    @Override
    public List<ChannelTreeVo> getChannelTree() {
        LambdaQueryWrapper<NvrChannel> lqw = Wrappers.lambdaQuery();
        lqw.isNotNull(NvrChannel::getChannelGroup);
        List<NvrChannelVo> channelVoList = baseMapper.selectVoList(lqw);
        if (channelVoList.isEmpty()) {
            return null;
        }
        Set<String> nvrIdSet = StreamUtils.toSet(channelVoList, NvrChannelVo::getNvrId);
        Map<String, Integer> statusMap = new HashMap<>();
        for (String nvrId : nvrIdSet) {
            List<NvrChannelJson> jsonList = nvrService.getVoidList(nvrId);
            statusMap.putAll(StreamUtils.toMap(jsonList, e -> nvrId + "_" + e.getId(), NvrChannelJson::getStatus));
        }
        Map<String, String> dict = dictService.getAllDictByDictType("channel_group");
        Map<String, List<NvrChannelVo>> groupMap = StreamUtils.groupByKey(channelVoList, NvrChannelVo::getChannelGroup);
        List<ChannelTreeVo> channelTreeVoList = new ArrayList<>();
        groupMap.forEach((key, value) -> {
            ChannelTreeVo treeVo = new ChannelTreeVo();
            treeVo.setId(key);
            treeVo.setLabel(dict.get(key));
            treeVo.setType("device");
            List<ChannelTreeVo> children = new ArrayList<>();
            value.forEach(item -> {
                ChannelTreeVo treeVo2 = new ChannelTreeVo();
                treeVo2.setId(item.getId().toString());
                String groupName = item.getChannelName();
                treeVo2.setLabel(groupName);
                treeVo2.setChannelId(item.getChannelId());
                treeVo2.setNvrId(item.getNvrId());
                treeVo2.setType("camera");
                Integer status = 99;
                String statusKey = item.getNvrId() + "_" + item.getChannelId();
                if (statusMap.get(statusKey) != null) {
                    status = statusMap.get(statusKey);
                }
                treeVo2.setStatus(status);
                children.add(treeVo2);
            });
            treeVo.setChildren(children);
            channelTreeVoList.add(treeVo);
        });
        return channelTreeVoList;
    }
}
