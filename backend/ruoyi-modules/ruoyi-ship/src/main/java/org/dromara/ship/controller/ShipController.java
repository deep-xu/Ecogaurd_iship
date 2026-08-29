package org.dromara.ship.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.ship.domain.bo.ShipBo;
import org.dromara.ship.domain.vo.ShipVo;
import org.dromara.ship.service.IShipService;
import org.dromara.ship.util.ThirdApi;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 *  轮船管理
 *
 * @author Lion Li
 * @date 2025-01-10
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ship/ship")
public class ShipController extends BaseController {

    private final IShipService shipService;

    private final ThirdApi thirdApi;
    /**
     * 大屏树型菜单
     */
    @SaCheckPermission("ship:ship:tree")
    @GetMapping("/tree")
    public R<List<ShipVo>> tree() {
        return R.ok(shipService.queryTreeList());
    }


    /**
     * 分页查询轮船信息
     */
    @SaCheckPermission("ship:ship:list")
    @GetMapping("/list")
    public TableDataInfo<ShipVo> list(ShipBo bo, PageQuery pageQuery) {
        return shipService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【轮船信息】列表
     */
    @SaCheckPermission("ship:ship:export")
    @Log(title = "【导出轮船信息】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ShipBo bo, HttpServletResponse response) {
        List<ShipVo> list = shipService.queryList(bo);
        ExcelUtil.exportExcel(list, "【导出轮船信息】", ShipVo.class, response);
    }

    /**
     * 获取【通过ID查询轮船】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("ship:ship:query")
    @GetMapping("/{id}")
    public R<ShipVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(shipService.queryById(id));
    }

    /**
     * 新增【轮船信息】
     */
    @SaCheckPermission("ship:ship:add")
    @Log(title = "【新增轮船信息】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ShipBo bo) {
        return toAjax(shipService.insertByBo(bo));
    }

    /**
     * 修改【轮船信息】
     */
    @SaCheckPermission("ship:ship:edit")
    @Log(title = "【修改轮船信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ShipBo bo) {
        return toAjax(shipService.updateByBo(bo));
    }

    /**
     * 删除【轮船信息】
     *
     * @param ids 主键串
     */
    @SaCheckPermission("ship:ship:remove")
    @Log(title = "【删除【轮船信息】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(shipService.deleteWithValidByIds(List.of(ids), true));
    }


    /**
     * 检测地址和端口是否可用
     */
    @Log(title = "检测地址和端口是否可用", businessType = BusinessType.OTHER)
    @GetMapping("/isPortAvailable")
    public R<Void> isPortAvailable(String host , int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            return R.ok("The test address port is available");
        } catch (IOException e) {
            return R.fail("The test address port is not available");
        }

    }

    /**
     * 检查上位机后台服务是否可用
     */

    @Log(title = "检查上位机后台服务是否可用", businessType = BusinessType.OTHER)
    @GetMapping("/isServerAvailable")
    public R<Void> isServerAvailable(String ak, String sk, String host, String port) {
        String str = thirdApi.getTokenJsonStr(ak, sk, host, port);
        JSONObject obj = JSONUtil.parseObj(str);
        if (obj.getInt("error_code")!= 0) {
            return R.fail("the service is not available, " + obj.getStr("error_msg"));
        } else {
            return R.ok("the service is available");
        }
    }

}
