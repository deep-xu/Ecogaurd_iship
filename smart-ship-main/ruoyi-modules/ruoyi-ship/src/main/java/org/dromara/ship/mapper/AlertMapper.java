package org.dromara.ship.mapper;

import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ship.domain.Alert;
import org.dromara.ship.domain.vo.AlertVo;
import org.dromara.ship.domain.vo.DeviceAlertStatVO;

import java.util.List;

/**
 * 【告警信息】Mapper接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id")
})
public interface AlertMapper extends BaseMapperPlus<Alert, AlertVo> {

    List<DeviceAlertStatVO> queryDeviceAlertStat();
}
