package org.dromara.ship.service;

import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【轮船信息】Service接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface IShipService {

    /**
     * 查询【轮船信息】
     *
     * @param id 主键
     * @return 【轮船信息】
     */
    ShipVo queryById(Long id);

    /**
     * 分页查询【轮船信息】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【轮船信息】分页列表
     */
    TableDataInfo<ShipVo> queryPageList(ShipBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的【轮船信息】列表
     *
     * @param bo 查询条件
     * @return 【轮船信息】列表
     */
    List<ShipVo> queryList(ShipBo bo);

    /**
     * 新增【轮船信息】
     *
     * @param bo 【轮船信息】
     * @return 是否新增成功
     */
    Boolean insertByBo(ShipBo bo);

    /**
     * 修改【轮船信息】
     *
     * @param bo 【轮船信息】
     * @return 是否修改成功
     */
    Boolean updateByBo(ShipBo bo);

    /**
     * 校验并批量删除【轮船信息】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<ShipVo> queryTreeList();
}
