package org.dromara.ship.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ship.domain.Device;
import org.dromara.ship.domain.vo.DeviceVo;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface DeviceMapper extends BaseMapperPlus<Device, DeviceVo> {

   /* @Param("page") Page<Project> page, @Param(Constants.WRAPPER)
    QueryWrapper<Project> lqw*/
    Page<DeviceVo> selectShipVoPage(@Param("page") Page<Device> build,  @Param(Constants.WRAPPER) LambdaQueryWrapper<Device> lqw);
}
