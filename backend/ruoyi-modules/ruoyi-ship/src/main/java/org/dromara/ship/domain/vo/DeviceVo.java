package org.dromara.ship.domain.vo;

import cn.hutool.json.JSONObject;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.ship.domain.Device;

import java.io.Serial;
import java.io.Serializable;



/**
 * 【请填写功能名称】视图对象 device
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Device.class)
public class DeviceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String id;

    /**
     * 设备名称
     */
    @ExcelProperty(value = "设备名称")
    private String name;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * ip地址
     */
    @ExcelProperty(value = "ip地址")
    private String ipv4;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long inferFreq;

    /**
     * 最大数据源
     */
    @ExcelProperty(value = "最大数据源")
    private Long maxSource;

    /**
     * 硬件版本
     */
    @ExcelProperty(value = "硬件版本")
    private String hardwareVersion;

    /**
     * 软件版本
     */
    @ExcelProperty(value = "软件版本")
    private String softwareVersion;

    /**
     * 在线状态
     */
    @ExcelProperty(value = "在线状态")
    private String status;

    /**
     * 船ID
     */
    @ExcelProperty(value = "船ID")
    private Long shipId;

    @ExcelProperty(value = "船名称")
    private String shipName;

    @ExcelProperty(value = "数据源")
    private JSONObject source;

}
