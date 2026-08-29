package org.dromara.common.netty.WSBean;


import jakarta.validation.constraints.NotNull;

/**
 * WEBSocket错误码
 */
public enum WebsocketCodeEnum {
    /**
     * 响应成功
     */
    SUCCESS(0, "Succeed"),

    /**
     * 通用错误
     */
    COMMON_ERROR(1, "Common error"),

    /**
     * 参数错误
     */
    INVALID_ARGUMENTS(2, "Invalid Arguments"),

    /**
     * 鉴权失败
     */
    NOT_AUTHORIZED(3, "Not Authorized"),

    /**
     * 功能不支持
     */
    NOT_SUPPORTED(4, "Not Supported"),

    /**
     * 用户状态非法
     */
    AbNORMAL_USER_STASUS(5, "Abnormal User Status"),

    /**
     * 部分成功
     */
    PARTIALLY_SUCCEED(102, "Partially Succeed"),

    /**
     * token异常或过期
     */
    INVALID_TOKEN(1002, "Invalid token"),

    /**
     * 设备不存在
     */
    DEVICE_NOT_EXIST(2001, "Device not exist"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    /**
     * 属性-code
     */
    private long code;

    /**
     * 属性-message
     */
    private String message;

    WebsocketCodeEnum(long code1, String message1) {
        this.code = code1;
        this.message = message1;
    }

    /**
     * 获取变量-code
     *
     * @return Integer
     *
     */
    public Long getCode() {
        return code;
    }

    /**
     * 获取变量-message
     *
     * @return String
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * 把错误码转为对应的具体信息
     *
     * @param code 错误码
     * @return CodeEnum
     *
     */
    public static CodeEnum lapiCodeTypeToUcs(@NotNull Long code) {
        CodeEnum codeEnum;

        if (code == WebsocketCodeEnum.NOT_SUPPORTED.code) {
            codeEnum = CodeEnum.FUNCTION_NOT_SUPPORTED;
        } else if (code == WebsocketCodeEnum.INVALID_ARGUMENTS.code) {
            codeEnum = CodeEnum.INVALID_PARAM;
        } else if (code == WebsocketCodeEnum.NOT_AUTHORIZED.code) {
            codeEnum = CodeEnum.NO_PERMISSION;
        } else {
            codeEnum = CodeEnum.COMMON_SERVER_ERROR;
        }
        return codeEnum;
    }

    /**
     * 错误码转换
     *
     * @param code 错误码
     * @return CodeEnum
     *
     */
    public static WebsocketCodeEnum UcsCodeTypeToLapi(@NotNull Integer code) {
        WebsocketCodeEnum codeEnum;

        if (code.equals(CodeEnum.FUNCTION_NOT_SUPPORTED.getCode())) {
            codeEnum = WebsocketCodeEnum.NOT_SUPPORTED;
        } else if (code.equals(CodeEnum.INVALID_PARAM.getCode())) {
            codeEnum = WebsocketCodeEnum.INVALID_ARGUMENTS;
        } else if (code.equals(CodeEnum.NO_PERMISSION.getCode())) {
            codeEnum = WebsocketCodeEnum.NOT_AUTHORIZED;
        } else {
            codeEnum = WebsocketCodeEnum.COMMON_ERROR;
        }
        return codeEnum;
    }
}
