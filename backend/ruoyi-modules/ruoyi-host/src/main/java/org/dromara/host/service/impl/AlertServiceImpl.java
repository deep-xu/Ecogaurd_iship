package org.dromara.host.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.host.domain.Alert;
import org.dromara.host.domain.bo.AlertBo;
import org.dromara.host.domain.vo.AlertVo;
import org.dromara.host.mapper.AlertMapper;
import org.dromara.host.service.IAlertService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 告警Service业务层处理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@RequiredArgsConstructor
@Service
public class AlertServiceImpl implements IAlertService {

    private final AlertMapper baseMapper;

    /**
     * 查询告警
     *
     * @param id 主键
     * @return 【通过ID查询告警信息】
     */
    @Override
    public AlertVo queryById(String id){
        return baseMapper.selectVoById(id);
    }



    /**
     * 分页查询告警列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 告警分页列表
     */
    @Override
    public TableDataInfo<AlertVo> queryPageList(AlertBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Alert> lqw = buildQueryWrapper(bo);
        Page<AlertVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的告警列表
     *
     * @param bo 查询条件
     * @return 告警列表
     */
    @Override
    public List<AlertVo> queryList(AlertBo bo) {
        LambdaQueryWrapper<Alert> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }


    @Override
    public List<AlertVo> findPendingList() {
        // 动态计算重试间隔：基础间隔 * 2^重试次数（最大24小时）
       /* String retryDelayCalc = """
        CASE
            WHEN last_try_time IS NULL THEN 0
            ELSE EXTRACT(EPOCH FROM (NOW() - last_try_time)) - (300 * POWER(2, LEAST(retry_count, 10)))
        END
        """;*/

        return baseMapper.createQuery();
            //.setParameter("retryDelay", retryDelayCalc)
            //.setMaxResults(50) // 每次同步批次大小
            //.getResultList();
    }


    private LambdaQueryWrapper<Alert> buildQueryWrapper(AlertBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Alert> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAlertTime() != null, Alert::getAlertTime, bo.getAlertTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDeviceId()), Alert::getDeviceId, bo.getDeviceId());
        lqw.like(StringUtils.isNotBlank(bo.getDeviceName()), Alert::getDeviceName, bo.getDeviceName());
        lqw.eq(StringUtils.isNotBlank(bo.getDeviceDesc()), Alert::getDeviceDesc, bo.getDeviceDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceId()), Alert::getSourceId, bo.getSourceId());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceIpv4()), Alert::getSourceIpv4, bo.getSourceIpv4());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceDesc()), Alert::getSourceDesc, bo.getSourceDesc());
        lqw.like(StringUtils.isNotBlank(bo.getAlgName()), Alert::getAlgName, bo.getAlgName());
        lqw.like(StringUtils.isNotBlank(bo.getAlgChName()), Alert::getAlgChName, bo.getAlgChName());
        lqw.eq(StringUtils.isNotBlank(bo.getHazardLevel()), Alert::getHazardLevel, bo.getHazardLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getImage()), Alert::getImage, bo.getImage());
        lqw.eq(StringUtils.isNotBlank(bo.getReservedData()), Alert::getReservedData, bo.getReservedData());
        lqw.eq(StringUtils.isNotBlank(bo.getVideo()), Alert::getVideo, bo.getVideo());
        lqw.eq(bo.getStatus() != null, Alert::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增告警
     *
     * @param bo 告警
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AlertBo bo) {
        Alert add = MapstructUtils.convert(bo, Alert.class);
        //validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改告警
     *
     * @param bo 告警
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AlertBo bo) {
        Alert update = MapstructUtils.convert(bo, Alert.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Alert entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除告警信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        //TODO 做一些业务上的校验,判断是否需要校验
        return baseMapper.deleteByIds(ids) > 0;
    }


}
