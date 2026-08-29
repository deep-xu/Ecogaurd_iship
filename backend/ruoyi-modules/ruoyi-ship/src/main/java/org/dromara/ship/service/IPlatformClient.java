package org.dromara.ship.service;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Header;

import java.util.List;

/**
 * @author lyn
 */
public interface IPlatformClient {


    @Get("http://{4}:{5}/ks/proxy/user/token?signature={0}&ak={1}&timestamp={2}&nonce={3}")
    String getToken(String signature, String ak , long timestamp , String nonce , String ip, String port1 );

    @Get("http://{2}:{3}/ks/proxy/source?device_id={1}")
    String getSource(@Header("Authorization") String authorization, String deviceId, String ip ,String port1);

    @Get("http://{1}:{2}/ks/proxy/device")
    String getDevice(@Header("Authorization") String authorization, String ip ,String port1);

    @Get("http://{3}:{4}/ks/proxy/stream/live/subscribe?device_id={1}&source_id={2}")
    String getStream(@Header("Authorization") String authorization, String deviceId, String sourceId ,String ip, String port2);

    @Get("http://{4}:{5}/ks/proxy/stream/attr?device_id={1}&stream={2}&draw_size={3}")
    String getStreamAttr(@Header("Authorization") String authorization, String deviceId, String stream, int drawSize, String ip, String port2);

    @Get("http://{1}:{2}/ks/proxy/stream/alert?page={3}&size={4}&start_time={5}&end_time={6}")
    String getAlert(@Header("Authorization") String authorization, String ip ,String port1, int page,int size , long startTime);

    @Get("http://{1}:{2}/ks/proxy/stream/alert/count?page={3}&size={4}&start_time={5}&end_time={6}")
    String getAlertCount(@Header("Authorization") String authorization, String ip ,String port1, int page,int size , long startTime);
}

