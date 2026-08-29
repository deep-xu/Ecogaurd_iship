package org.dromara.ship.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
@TableName("nvr_info")
public class NvrInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 设备密码
     */
    private String password;

    /**
     * 名称
     */
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
