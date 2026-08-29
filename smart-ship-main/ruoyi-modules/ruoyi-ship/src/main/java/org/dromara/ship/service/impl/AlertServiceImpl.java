package org.dromara.ship.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.ship.domain.Alert;
import org.dromara.ship.domain.Device;
import org.dromara.ship.domain.bo.AlertBo;
import org.dromara.ship.domain.vo.AlertVo;
import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.mapper.AlertMapper;
import org.dromara.ship.mapper.DeviceMapper;
import org.dromara.ship.mapper.ShipMapper;
import org.dromara.ship.service.IAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
@Slf4j
public class AlertServiceImpl implements IAlertService {

    private final AlertMapper baseMapper;

    private final DeviceMapper deviceMapper;

    private final ShipMapper shipMapper;

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
        lqw.orderByDesc(Alert::getCreateTime);
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




    private LambdaQueryWrapper<Alert> buildQueryWrapper(AlertBo bo) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAlert(MultipartFile file, Alert alert) {
        try {
            // 1. 保存文件到服务器或云存储
            String fileName = file.getOriginalFilename();
            String imagePath = alert.getImage();
            String filePath = imagePath.substring(0,imagePath.lastIndexOf("/"));
            // 文件存储路径(根据实际配置调整)
            String uploadDir = filePath + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean b = dir.mkdirs();
                if(!b){
                    log.error("创建目录失败");
                    throw new ServiceException("创建目录失败");
                }
            }
            // 保存文件//newFileName
            String newfilePath = uploadDir + fileName;
            log.info("保存文件:{}", newfilePath);
            file.transferTo(new File(newfilePath));
            // 2. 保存告警信息
            alert.setImage(newfilePath);
            //MapstructUtils.convert(alertBo, alert);
            Device device = deviceMapper.selectById(alert.getDeviceId());
            if (device.getShipId() != null) {
                ShipVo shipVo = shipMapper.selectVoById(device.getShipId());
                if (shipVo.getDeptId() != null){
                    alert.setDeptId(shipVo.getDeptId());
                }
            }
            baseMapper.insert(alert);
        } catch (Exception e) {
            log.error("图片上传失败:{}", e.getMessage());
            throw new ServiceException("图片上传失败");
        }
    }


}
