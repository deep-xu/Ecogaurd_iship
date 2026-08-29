package org.dromara.ship.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.ship.domain.Ship;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * 【轮船信息】视图对象 ship
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Ship.class)
public class ShipVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * IMO编号
     */
    @ExcelProperty(value = "IMO编号")
    private String imoNum;

    /**
     * 上位机平台地址
     */
    @ExcelProperty(value = "上位机平台地址")
    private String platform;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String description;


    private List<DeviceVo> deviceVoList;


    @ExcelProperty(value = "ak")
    private String ak;

    @ExcelProperty(value = "sk")
    private String sk;

    @ExcelProperty(value = "上位机平台端口1")
    private String port1;

    @ExcelProperty(value = "上位机平台端口2")
    private String port2;

    @ExcelProperty(value = "上位机平台端口3")
    private String port3;

    /**
     * 部门名称
     */
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    private Long deptName;
}


