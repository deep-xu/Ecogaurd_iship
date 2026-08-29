package org.dromara.ship.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.ship.domain.NvrChannel;
import org.dromara.ship.domain.bo.NvrChannelBo;
import org.dromara.ship.domain.vo.ChannelTreeVo;
import org.dromara.ship.domain.vo.NvrChannelVo;

import java.util.Collection;
import java.util.List;

/**
 * NVR通道信息Service接口
 *
 * @author 杨斌
 * @date 2025-10-23
 */
public interface INvrChannelService {

    /**
     * 查询NVR通道信息
     *
     * @param id 主键
     * @return NVR通道信息
     */
    NvrChannelVo queryById(Long id);

    /**
     * 分页查询NVR通道信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return NVR通道信息分页列表
     */
    TableDataInfo<NvrChannelVo> queryPageList(NvrChannelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的NVR通道信息列表
     *
     * @param bo 查询条件
     * @return NVR通道信息列表
     */
    List<NvrChannelVo> queryList(NvrChannelBo bo);

    /**
     * 新增NVR通道信息
     *
     * @param bo NVR通道信息
     * @return 是否新增成功
     */
    Boolean insertByBo(NvrChannelBo bo);

    /**
     * 修改NVR通道信息
     *
     * @param bo NVR通道信息
     * @return 是否修改成功
     */
    Boolean updateByBo(NvrChannelBo bo);

    /**
     * 校验并批量删除NVR通道信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 批量保存
     */
    void batchSave(Collection<NvrChannel> nvrChannels);

    /**
     * 根据NVR删除通道
     */
    void deleteWithNvrId(String nvrId);

    /**
     * 根据分组获取通道信息
     */
    List<ChannelTreeVo> getChannelTree();
}
