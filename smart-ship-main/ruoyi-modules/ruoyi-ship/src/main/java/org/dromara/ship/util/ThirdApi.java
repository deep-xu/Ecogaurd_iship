package org.dromara.ship.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.PlatformConstants;
import org.dromara.ship.service.IPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ThirdApi {


    private final IPlatformClient platformClient;

    @Autowired
    public ThirdApi(IPlatformClient platformClient) {
        this.platformClient = platformClient;
    }

    public  String getToken(String ak, String sk, String ip, String port1) {
        long timestamp = System.currentTimeMillis() / 1000;
        String sign = SignatureGenerator.generateSignature(sk, ak, timestamp, PlatformConstants.NONCE);
        JSONObject json  = JSONUtil.parseObj(platformClient.getToken(sign, ak, timestamp,PlatformConstants.NONCE, ip , port1));
        return json.getStr("data");
    }


    public  String getTokenJsonStr(String ak, String sk, String ip, String port1) {
        long timestamp = System.currentTimeMillis() / 1000;
        String sign = SignatureGenerator.generateSignature(sk, ak, timestamp, PlatformConstants.NONCE);
        JSONObject json  = JSONUtil.parseObj(platformClient.getToken(sign, ak, timestamp,PlatformConstants.NONCE, ip , port1));
        return json.toString();

    }


}
