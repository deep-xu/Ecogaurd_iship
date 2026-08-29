package org.dromara.common.netty.config;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class WSOperator {
    public static Long cseq = 1L;

    static HashMap<String, CompletableFuture<WebsocketRsp>> responseFutureMap = new HashMap<>();


    public static WebsocketRsp sendLapiRequest(ChannelHandlerContext wsContext, String url, String method, Object data) {
        if (wsContext.isRemoved()) {
            System.out.println("该通道已断开链接");
            return null;
        }
        CompletableFuture<WebsocketRsp> future = new CompletableFuture<>();
        responseFutureMap.put(cseq.toString(), future);

        WebsocketReq websocketReq = new WebsocketReq();
        websocketReq.setRequestURL(url);
        websocketReq.setMethod(method);
        websocketReq.setCseq(cseq);
        websocketReq.setData(data);

        String msg = JsonUtils.toJsonString(websocketReq);
        System.out.println("发送请求：" + msg);
        wsContext.channel().writeAndFlush(new TextWebSocketFrame(msg).retain());

        cseq++;

        // 5秒后没有响应，则为超时
        new Thread(() -> {
            try {
                Thread.sleep(10000L);
                if (!future.isDone()) {
                    System.out.println("请求超时");
                    future.complete(null);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        try {
            WebsocketRsp response = future.get();
            // System.out.println("收到响应：" + response.getData().toString()); // 输出：Hello, CompletableFuture!
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void OnReceiveResponse(WebsocketRsp response) {
        Long cseq = response.getCseq();
        if (responseFutureMap.containsKey(cseq.toString())) {
            CompletableFuture<WebsocketRsp> future = responseFutureMap.get(cseq.toString());
            future.complete(response);
            responseFutureMap.remove(cseq.toString());
        } else {
            System.out.println("Receive response with cseq: " + cseq + " but not found in responseFutureMap");
            System.out.println(response.toString());
        }
    }

}
