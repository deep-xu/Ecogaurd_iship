package org.dromara.host.service;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.host.component.NetworkChecker;
import org.dromara.host.constant.Constants;
import org.dromara.host.domain.Alert;
import org.dromara.host.domain.bo.AlertBo;
import org.dromara.host.domain.vo.AlertVo;
import org.dromara.host.service.IAlertService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lyn
 */
@Service
@RequiredArgsConstructor
public class AlertDataSyncService {
    private final IAlertService alertService;
    private final CloudApiClient cloudApi;
    private final NetworkChecker networkChecker;

    @Scheduled(fixedRateString = "${sync.interval:5000}")
    @Transactional
    public void syncDataToCloud() {
        if (!networkChecker.isOnline()) {
            return;
        }

        // 获取待同步数据（按重试次数排序，失败次数多的优先）
        List<AlertVo> pendingData = alertService.findPendingList();

        pendingData.forEach(data -> {
            AlertBo bo = Objects.requireNonNull(MapstructUtils.convert(data, AlertBo.class));
            try {
                boolean rs1 = cloudApi.uploadData(BeanUtil.beanToMap(data));
                //File imageFile = new File(bo.getImage());
                //boolean rs2 = cloudApi.uploadImage(imageFile);
                if (rs1) {
                    data.setSyncStatus(Constants.SycnStatus.SUCCESS.getCode());

                } else {
                    updateFailedRecord(bo);
                }
                //alertService.insertByBo(MapstructUtils.convert(data, AlertBo.class));
            } catch (Exception e) {
                //data.setSyncStatus(Constants.SycnStatus.FAIL.getCode());
                //alertService.insertByBo(MapstructUtils.convert(data, AlertBo.class) );
                updateFailedRecord(bo);
            }
            alertService.insertByBo(MapstructUtils.convert(data, AlertBo.class));
        });


    }

    private void updateFailedRecord(AlertBo data) {
        if(data.getRetryCount()==null){
            data.setRetryCount(0);
        }
        data.setRetryCount(data.getRetryCount() + 1);
        data.setLastTryTime(new Date().getTime());
        if (data.getRetryCount() >= 3) {
            data.setSyncStatus(Constants.SycnStatus.FAILED.getCode());
        }
    }
}
