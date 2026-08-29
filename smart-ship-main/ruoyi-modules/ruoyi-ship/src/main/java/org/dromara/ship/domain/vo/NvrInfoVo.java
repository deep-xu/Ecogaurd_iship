package org.dromara.ship.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
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
@ExcelIgnoreUnannotated
@AutoMapper(target = NvrInfo.class)
public class NvrInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Nvr设备ID
     */
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
     * 连接IP
     */
    private String ip;


    /**
     * 船ID
     */
    private Long shipId;

    /**
     * 船名
     */
    private String shipName;
}
