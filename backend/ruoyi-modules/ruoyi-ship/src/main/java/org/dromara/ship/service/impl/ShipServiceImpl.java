package org.dromara.ship.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.ship.domain.Device;
import org.dromara.ship.mapper.DeviceMapper;
import org.springframework.stereotype.Service;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.domain.Ship;
import org.dromara.ship.mapper.ShipMapper;
import org.dromara.ship.service.IShipService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【轮船信息】Service业务层处理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@RequiredArgsConstructor
@Service
public class ShipServiceImpl implements IShipService {

    private final ShipMapper baseMapper;

    private final DeviceMapper deviceMapper;

    /**
     * 查询【通过ID查询轮船信息】
     *
     * @param id 主键
     * @return 【轮船信息】
     */
    @Override
    public ShipVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询【轮船信息】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【轮船信息】分页列表
     */
    @Override
    public TableDataInfo<ShipVo> queryPageList(ShipBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Ship> lqw = buildQueryWrapper(bo);
        Page<ShipVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的【轮船信息】列表
     *
     * @param bo 查询条件
     * @return 【轮船信息】列表
     */
    @Override
    public List<ShipVo> queryList(ShipBo bo) {
        LambdaQueryWrapper<Ship> lqw = buildQueryWrapper(bo);
        baseMapper.selectList();
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Ship> buildQueryWrapper(ShipBo bo) {
        //Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Ship> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), Ship::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getImoNum()), Ship::getImoNum, bo.getImoNum());
        lqw.eq(StringUtils.isNotBlank(bo.getPlatform()), Ship::getPlatform, bo.getPlatform());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), Ship::getDescription, bo.getDescription());
        return lqw;
    }

    /**
     * 新增【轮船信息】
     *
     * @param bo 【轮船信息】
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ShipBo bo) {
        Ship add = MapstructUtils.convert(bo, Ship.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【轮船信息】
     *
     * @param bo 【轮船信息】
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ShipBo bo) {
        Ship update = MapstructUtils.convert(bo, Ship.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Ship entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除【轮船信息】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<ShipVo> queryTreeList() {
        List<ShipVo> shipVos = baseMapper.selectVoList();

        shipVos.forEach(shipVo -> {

            shipVo.setDeviceVoList(deviceMapper.selectVoList(new LambdaQueryWrapper<Device>().eq(Device::getShipId, shipVo.getId())));
        });
        return shipVos;
    }
}
