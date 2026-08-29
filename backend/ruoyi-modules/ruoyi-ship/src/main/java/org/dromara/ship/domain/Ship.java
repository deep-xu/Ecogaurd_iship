package org.dromara.ship.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 【轮船信息】对象 ship
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ship")
public class Ship extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 名称
     */
    private String name;

    /**
     * IMO编号
     */
    private String imoNum;

    /**
     * 上位机平台地址
     */
    private String platform;

    /**
     * 描述
     */
    private String description;


    /**
     * ak
     */
    private String ak;

    /*
     * sk
     */
    private String sk;

    /*
     * 上位机平台端口1
     */
    private String port1;

    /*
     * 上位机平台端口2
     */
    private String port2;

    /*
     * 上位机平台端口3
     */
    private String port3;

}
