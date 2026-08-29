package org.dromara.ship.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.web.core.BaseController;
import org.dromara.ship.config.MqttTemplate;
import org.dromara.ship.domain.Alert;
import org.dromara.ship.domain.bo.AlertBo;
import org.dromara.ship.domain.vo.AlertVo;
import org.dromara.ship.service.IAlertService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.StrUtil.uuid;

/**
 * 告警管理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
//@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ship/alert")
@SaIgnore
@Slf4j
public class AlertController extends BaseController {

    private final IAlertService alertService;
    private final MqttTemplate mqttTemplate;

    /**
     * 查询【分页查询告警信息】列表
     */

    @GetMapping("/list")
    public TableDataInfo<AlertVo> list(AlertBo bo, PageQuery pageQuery) {
        return alertService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【告警信息】列表
     */

    @Log(title = "【导出告警信息】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AlertBo bo, HttpServletResponse response) {
        List<AlertVo> list = alertService.queryList(bo);
        ExcelUtil.exportExcel(list, "【告警信息】", AlertVo.class, response);
    }

    /**
     * 获取【告警】详细信息
     *
     * @param id 主键
     */

    @GetMapping("/get")
    public R<AlertVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String id) {
        return R.ok(alertService.queryById(id));
    }

    /**
     * 上传告警信息
     * @param file
     * @param alert
     * @return
     */
    @PostMapping("/upload")
    public R<Void> upload(@RequestPart("file") MultipartFile file, @RequestPart("alert") Alert alert) {
        try {
            // 参数校验
            if (file.isEmpty()) {
                return R.fail("上传文件不能为空");
            }

            // 文件大小限制(例如: 10MB)
            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return R.fail("文件大小不能超过10MB");
            }

            // 文件类型限制(根据需要调整)
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") )) {
                return R.fail("只允许上传图片文件");
            }
            log.info("开始上传文件: {}", file.getOriginalFilename());
            DataPermissionHelper.ignore(()->alertService.saveAlert(file, alert));
            String payload = "{\"msg_id\":\"" + uuid() + "\",\"msg_type\":\"" + "alert" + "\",\"time\":\"" + System.currentTimeMillis() + "\"}";

            JSONObject jsonObject = JSONUtil.parseObj(payload);
            Map<String, Object> alertMap = BeanUtil.beanToMap(alert);
            alertMap.remove("id");
            alertMap.remove("video");
            Map<String, Object> underlineMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : alertMap.entrySet()) {
                underlineMap.put(StrUtil.toUnderlineCase(entry.getKey()), entry.getValue());
            }
            jsonObject.set("data", underlineMap);
            mqttTemplate.sendToTopic(jsonObject.toString(), "ks/proxy/alert/"+alert.getDeviceId());
            return R.ok("告警保存成功");
        } catch (Exception e) {

            return R.fail("告警保存失败: " + e.getMessage());
        }
    }




}
