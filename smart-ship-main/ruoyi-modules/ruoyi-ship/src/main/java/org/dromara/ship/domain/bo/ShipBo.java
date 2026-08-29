package org.dromara.ship.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.ship.domain.Ship;

/**
 * 【轮船信息】业务对象 ship
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Ship.class, reverseConvertGenerate = false)
public class ShipBo extends BaseEntity {

    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /**
     * IMO编号
     */
    @NotBlank(message = "IMO编号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String imoNum;

    /**
     * 上位机平台地址
     */
    @NotBlank(message = "上位机平台IP地址不能为空", groups = {AddGroup.class, EditGroup.class})
    private String platform;

    /**
     * 描述
     */
    //@NotBlank(message = "描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String description;

    private String ak;

    private String sk;

    private String port1;

    private String port2;

    private String port3;
}

