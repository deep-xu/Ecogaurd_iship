package org.dromara.common.netty.WSBean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * 响应体
 */
@Data
public class WebsocketRsp {

    /**
     * 响应url
     */
    @JsonProperty("ResponseURL")
    private String responseURL;

    /**
     * 响应码
     */
    @JsonProperty("ResponseCode")
    private Long responseCode;

    /**
     * 响应语
     */
    @JsonProperty("ResponseString")
    private String responseString;

    /**
     * 序号
     */
    @JsonProperty("Cseq")
    private Long cseq;

    /**
     * 响应数据
     */
    @JsonProperty("Data")
    private Object data;

    public WebsocketRsp() {
        this.responseCode = WebsocketCodeEnum.SUCCESS.getCode();
        this.responseString = WebsocketCodeEnum.SUCCESS.getMessage();
    }

}
