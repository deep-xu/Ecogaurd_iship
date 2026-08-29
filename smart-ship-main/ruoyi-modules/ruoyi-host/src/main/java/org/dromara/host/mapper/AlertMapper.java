package org.dromara.host.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.host.domain.Alert;
import org.dromara.host.domain.vo.AlertVo;

import java.util.List;

/**
 * 【告警信息】Mapper接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface AlertMapper extends BaseMapperPlus<Alert, AlertVo> {


    List<AlertVo> createQuery(
        //@Param("page") Page<AlertVo> page, @Param("ew") Wrapper<AlertVo> wrapper
    );

}
