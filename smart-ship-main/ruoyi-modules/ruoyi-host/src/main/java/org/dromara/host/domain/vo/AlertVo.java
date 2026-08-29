package org.dromara.host.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.host.domain.Alert;

import java.io.Serial;
import java.io.Serializable;


/**
 * 【请填写功能名称】视图对象 alert
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Alert.class)
public class AlertVo implements Serializable {



    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String id;

    /**
     * 告警时间
     */
    @ExcelProperty(value = "告警时间")
    private Long alertTime;

    /**
     * 设备id
     */
    @ExcelProperty(value = "设备id")
    private String deviceId;

    /**
     * 设备名称
     */
    @ExcelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String deviceDesc;

    /**
     * 数据源id
     */
    @ExcelProperty(value = "数据源id")
    private String sourceId;

    /**
     * 数据源IP
     */
    @ExcelProperty(value = "数据源IP")
    private String sourceIpv4;

    /**
     * 数据源描述
     */
    @ExcelProperty(value = "数据源描述")
    private String sourceDesc;

    /**
     * 算法名称
     */
    @ExcelProperty(value = "算法名称")
    private String algName;

    /**
     * 算法通道名称
     */
    @ExcelProperty(value = "算法通道名称")
    private String algChName;

    /**
     * 危险等级
     */
    @ExcelProperty(value = "危险等级")
    private String hazardLevel;

    /**
     * 图片路径
     */
    @ExcelProperty(value = "图片路径")
    private String image;

    /**
     * 图片路径Url
     */
    //@Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "image")
    private String imageUrl;
    /**
     *
     */
    @ExcelProperty(value = "")
    private String reservedData;

    /**
     * 视频路径
     */
    @ExcelProperty(value = "视频路径")
    private String video;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private Integer status;


    /**
     * 同步状态
     */
    @ExcelProperty(value = "同步状态")
    private Integer syncStatus;
}
