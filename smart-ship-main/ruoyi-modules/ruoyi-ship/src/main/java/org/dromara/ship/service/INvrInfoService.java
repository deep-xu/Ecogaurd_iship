package org.dromara.ship.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.ship.domain.bo.NvrChannelJson;
import org.dromara.ship.domain.bo.NvrInfoBo;
import org.dromara.ship.domain.vo.NvrInfoVo;

import java.util.Collection;
import java.util.List;

/**
 * Nvr设备Service接口
 *
 * @author Lion Li
 * @date 2025-01-10
 */
public interface INvrInfoService {

    /**
     * 查询【Nvr设备】
     *
     * @param id 主键
     * @return 【Nvr设备】
     */
    NvrInfoVo queryById(String id);

    /**
     * 分页查询【Nvr设备】列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 【Nvr设备】分页列表
     */
    TableDataInfo<NvrInfoVo> queryPageList(NvrInfoBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的【Nvr设备】列表
     *
     * @param bo 查询条件
     * @return 【Nvr设备】列表
     */
    List<NvrInfoVo> queryList(NvrInfoBo bo);

    /**
     * 新增【Nvr设备】
     *
     * @param bo 【Nvr设备】
     * @return 是否新增成功
     */
    Boolean insertByBo(NvrInfoBo bo);

    /**
     * 修改【Nvr设备】
     *
     * @param bo 【Nvr设备】
     * @return 是否修改成功
     */
    Boolean updateByBo(NvrInfoBo bo);

    /**
     * 校验并批量删除【Nvr设备】信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);


    /**
     * 获取通道ID
     *
     * @param nvrId nvr设备ID
     */
    List<NvrChannelJson> getVoidList(String nvrId);
}
