package org.dromara.common.core.service;

/**
 * Nvr设备服务
 *
 * @author MrYang
 * @date 2025/10/26
 **/
public interface NvrService {
    /**
     * 校验设备信息
     *
     * @param nvrId NvrId
     */
    String checkNvrInfo(String nvrId);

    /**
     * 清理摄像头组
     */
    void cleanGroup(String groupValue);
}

