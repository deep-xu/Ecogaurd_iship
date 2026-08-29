package org.dromara.ship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.ship.domain.Device;
import org.dromara.ship.domain.vo.DeviceAlertStatVO;
import org.dromara.ship.domain.vo.DeviceVo;
import org.dromara.ship.domain.bo.DeviceBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.ship.domain.vo.ScreenVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 设备Service接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface IDeviceService extends IService<Device> {

    /**
     * 查询【请填写功能名称】
     *
     * @param id 主键
     * @return 【请填写功能名称】
     */
    DeviceVo queryById(String id);

    /**
     * 分页查询【请填写功能名称】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【请填写功能名称】分页列表
     */
    TableDataInfo<DeviceVo> queryPageList(DeviceBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的【请填写功能名称】列表
     *
     * @param bo 查询条件
     * @return 【请填写功能名称】列表
     */
    List<DeviceVo> queryList(DeviceBo bo);

    /**
     * 新增【请填写功能名称】
     *
     * @param bo 【请填写功能名称】
     * @return 是否新增成功
     */
    Boolean insertByBo(DeviceBo bo);

    /**
     * 修改【请填写功能名称】
     *
     * @param bo 【请填写功能名称】
     * @return 是否修改成功
     */
    Boolean updateByBo(DeviceBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);

    Boolean bind(String deviceId, Long shipId);

    Boolean unbind(String deviceId, Long shipId);

    List<DeviceAlertStatVO> queryDeviceAlertStat();

    Map<String, Integer> querySourceStat();
}
