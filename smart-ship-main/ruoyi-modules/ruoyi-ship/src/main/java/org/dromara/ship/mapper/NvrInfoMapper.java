package org.dromara.ship.mapper;

import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ship.domain.NvrInfo;
import org.dromara.ship.domain.vo.NvrInfoVo;

/**
 * Nvr设备Mapper接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id")
})
public interface NvrInfoMapper extends BaseMapperPlus<NvrInfo, NvrInfoVo> {


}
