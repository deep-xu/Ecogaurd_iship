package org.dromara.common.netty.WSThread;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.netty.WSBean.KeepAliveRspAO;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;

public class KeepLiveThread implements Runnable {


    private Channel channel;
    private String jsonObject;
    private static final int lives = 60;

    public KeepLiveThread(Channel channel, String jsonObject) {
        this.channel = channel;
        this.jsonObject = jsonObject;
    }

    @Override
    public void run() {
        requestReceive(channel, jsonObject);
    }

    private void requestReceive(Channel channel, String jsonObject) {
        String currentIP = channel.remoteAddress().toString();
        WebsocketReq websocketReq = JsonUtils.parseObject(jsonObject, WebsocketReq.class);
        WebsocketRsp websocketRsp = new WebsocketRsp();
        websocketRsp.setCseq(websocketReq.getCseq());
        websocketRsp.setResponseURL(websocketReq.getRequestURL());
        KeepAliveRspAO keepAliveRspAO = new KeepAliveRspAO();
        //设置保活时间
        keepAliveRspAO.setTimeout(lives);
        keepAliveRspAO.setTimestamp(System.currentTimeMillis() / 1000L);
        websocketRsp.setData(keepAliveRspAO);
        String msg = JsonUtils.toJsonString(websocketRsp);
//        System.out.println("服务器端响应给" + currentIP + "的保活：" + msg);
        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
