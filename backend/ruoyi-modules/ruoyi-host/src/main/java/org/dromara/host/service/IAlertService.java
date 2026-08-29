package org.dromara.host.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.host.domain.bo.AlertBo;
import org.dromara.host.domain.vo.AlertVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface IAlertService {

    /**
     * 查询【通过ID查询告警信息】
     *
     * @param id 主键
     * @return 【通过ID查询告警信息】
     */
    AlertVo queryById(String id);

    /**
     * 分页查询【查询告警信息】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【查询告警信息】分页列表
     */
    TableDataInfo<AlertVo> queryPageList(AlertBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的【】列表
     *
     * @param bo 查询条件
     * @return 【根据查询条件查询告警信息】列表
     */
    List<AlertVo> queryList(AlertBo bo);


    List<AlertVo> findPendingList();


    /**
     * 新增【新增告警信息】
     *
     * @param bo 【新增告警信息】
     * @return 结果
     */
    Boolean insertByBo(AlertBo bo);

    /**
     * 修改【修改告警信息】
     *
     * @param bo 【修改告警信息】
     * @return 结果
     */
    Boolean updateByBo(AlertBo bo);

    /**
     * 删除【删除告警信息】信息
     *
     * @param ids 主键串
     * @return 结果
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);




}
