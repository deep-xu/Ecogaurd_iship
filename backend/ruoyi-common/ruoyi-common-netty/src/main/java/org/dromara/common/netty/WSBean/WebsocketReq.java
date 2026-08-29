package org.dromara.common.netty.WSBean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * WEBSocket请求实体类
 *
 */
@Data
public class WebsocketReq {


    /**
     * 请求url
     */
    @JsonProperty("RequestURL")
    private String requestURL;

    /**
     * 请求方法
     */
    @JsonProperty("Method")
    private String method;

    /**
     * 请求序号
     */
    @JsonProperty("Cseq")
    private Long cseq;

    /**
     * 请求数据
     */
    @JsonProperty("Data")
    private Object data;
}
