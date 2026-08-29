package org.dromara.ship.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.ship.domain.vo.DeviceAlertStatVO;

import java.util.*;

import org.dromara.ship.mapper.AlertMapper;
import org.springframework.stereotype.Service;
import org.dromara.ship.domain.bo.DeviceBo;
import org.dromara.ship.domain.vo.DeviceVo;
import org.dromara.ship.domain.Device;
import org.dromara.ship.mapper.DeviceMapper;
import org.dromara.ship.service.IDeviceService;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@RequiredArgsConstructor
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    private final DeviceMapper baseMapper;

    private final AlertMapper alertMapper;

    /**
     * 查询【请填写功能名称】
     *
     * @param id 主键
     * @return 【请填写功能名称】
     */
    @Override
    public DeviceVo queryById(String id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询【请填写功能名称】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【请填写功能名称】分页列表
     */
    @Override
    public TableDataInfo<DeviceVo> queryPageList(DeviceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Device> lqw = buildQueryWrapper(bo);
        Page<DeviceVo> result = baseMapper.selectShipVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的【请填写功能名称】列表
     *
     * @param bo 查询条件
     * @return 【请填写功能名称】列表
     */
    @Override
    public List<DeviceVo> queryList(DeviceBo bo) {
        LambdaQueryWrapper<Device> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Device> buildQueryWrapper(DeviceBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Device> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), Device::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getDesc()), Device::getDescription, bo.getDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getIpv4()), Device::getIpv4, bo.getIpv4());
        lqw.eq(bo.getInferFreq() != null, Device::getInferFreq, bo.getInferFreq());
        lqw.eq(bo.getMaxSource() != null, Device::getMaxSource, bo.getMaxSource());
        lqw.eq(StringUtils.isNotBlank(bo.getHardwareVersion()), Device::getHardwareVersion, bo.getHardwareVersion());
        lqw.eq(StringUtils.isNotBlank(bo.getSoftwareVersion()), Device::getSoftwareVersion, bo.getSoftwareVersion());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Device::getStatus, bo.getStatus());
        lqw.eq(bo.getShipId() != null, Device::getShipId, bo.getShipId());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param bo 【请填写功能名称】
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DeviceBo bo) {
        Device add = MapstructUtils.convert(bo, Device.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param bo 【请填写功能名称】
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DeviceBo bo) {
        Device update = MapstructUtils.convert(bo, Device.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Device entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除【请填写功能名称】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public Boolean bind(String deviceId, Long shipId) {
        Device device = baseMapper.selectById(deviceId);
        if(device == null){
            return false;
        }
        device.setShipId(shipId);
        return baseMapper.updateById(device) > 0;
    }

    @Override
    public Boolean unbind(String deviceId, Long shipId) {

        Device device = baseMapper.selectById(deviceId);
        if(device == null){
            return false;
        }
        //device.setShipId(null);这样设置会无法更新为null值
        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Device::getShipId, null);
        return baseMapper.update(device, updateWrapper) > 0;
        //return baseMapper.updateById(device) > 0;
    }

    @Override
    public List<DeviceAlertStatVO> queryDeviceAlertStat() {

        return alertMapper.queryDeviceAlertStat();
    }

    @Override
    public Map<String, Integer>  querySourceStat() {
        List<DeviceVo> list = baseMapper.selectVoList();
        Map<String,Integer> maps = new HashMap<>();
        maps.put("Enable", 0);
        maps.put("Disable", 0);
        if (list == null || list.isEmpty()) {
            return maps;
        }
        list.stream().filter(Objects::nonNull)
             .map(DeviceVo::getSource)
             .forEach(jsonObject -> {
             Set<String> keys =  jsonObject.keySet();
                 keys.forEach(key -> {
                     JSONObject jsonObject1 = jsonObject.getJSONObject(key);
                     if(jsonObject1.getInt("status")==1){
                         maps.put("Enable", maps.get("Enable")+1);
                     }else {
                         maps.put("Disable", maps.get("Disable")+1);
                     };
                 });
            });
        return maps;
    }
}
