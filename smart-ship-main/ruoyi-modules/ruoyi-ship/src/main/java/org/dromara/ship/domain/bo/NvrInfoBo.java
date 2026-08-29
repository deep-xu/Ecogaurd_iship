package org.dromara.ship.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.ship.domain.NvrInfo;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 * Nvr信息
 *
 * @author: Mr.Yang
 * @create: 2025-10-02
 **/
@Data
@AutoMapper(target = NvrInfo.class, reverseConvertGenerate = false)
public class NvrInfoBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "设备ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private String id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 设备密码
     */
    @NotNull(message = "设备密码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String password;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /**
     * 描述
     */
    private String remark;

    /**
     * 船ID
     */
    private Long shipId;
}
