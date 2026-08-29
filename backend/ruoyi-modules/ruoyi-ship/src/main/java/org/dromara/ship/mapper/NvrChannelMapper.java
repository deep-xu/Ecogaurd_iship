package org.dromara.ship.mapper;

import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.vo.NvrChannelVo;

/**
 * NVR通道信息Mapper接口
 *
 * @author 杨斌
 * @date 2025-10-23
 */
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id")
})
public interface NvrChannelMapper extends BaseMapperPlus<NvrChannel, NvrChannelVo> {

}
