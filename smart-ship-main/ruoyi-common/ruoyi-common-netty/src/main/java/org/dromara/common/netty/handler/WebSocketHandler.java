package org.dromara.common.netty.handler;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.service.NvrService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.netty.WSBean.WebsocketReq;
import org.dromara.common.netty.WSBean.WebsocketRsp;
import org.dromara.common.netty.WSThread.KeepLiveThread;
import org.dromara.common.netty.config.KeepLiveThreadPoolExecutor;
import org.dromara.common.netty.config.WSOperator;
import org.dromara.common.netty.core.NettyEmitterManager;
import org.dromara.common.redis.utils.RedisUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    //用于websocket握手的处理类
    private WebSocketServerHandshaker handshaker;
    private NvrService nvrService;

    //注册接口
    private static final String LAPI_REGISTER = "/LAPI/V1.0/System/UpServer/Register";
    //保活接口
    private static final String LAPI_KEEPALIVE = "/LAPI/V1.0/System/UpServer/Keepalive";
    //关闭连接
    private static final String LAPI_UNREGISTER = "/LAPI/V1.0/System/UpServer/Unregister";

    public WebSocketHandler(NvrService nvrService) {
        this.nvrService = nvrService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
        throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                // websocket连接请求，以http请求接入
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                // websocket业务处理，处理websocket客户端的消息
                handleWebSocketRequest(ctx, (WebSocketFrame) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channle，并且放到ChannelGroup中去进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        String currentIP = addr.getAddress().getHostAddress();
        String channelId = ctx.channel().id().asLongText();
        System.out.println("设备获得连接,设备的IP地址为：" + currentIP);
        NettyEmitterManager.addChannel(channelId, ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        String channelId = ctx.channel().id().asLongText();
        String currentIP = addr.getAddress().getHostAddress();
        System.out.println("设备被移除,设备的IP地址为：" + currentIP);
        NettyEmitterManager.removeChannel(channelId);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //添加连接
        System.out.println("客户端加入连接：" + ctx.channel().remoteAddress() + "\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("客户端断开连接：" + ctx.channel().remoteAddress() + "\n");
        String channelId = ctx.channel().id().asLongText();
        NettyEmitterManager.removeChannel(channelId);
        String deviceCode = RedisUtils.getCacheObject(CacheNames.NVR_IP_ID + channelId);
        if (StringUtils.isNotEmpty(deviceCode)) {
            RedisUtils.deleteObject(CacheNames.NVR_ID_IP + deviceCode);
            RedisUtils.deleteObject(CacheNames.NVR_IP_ID + channelId);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 获取WebSocket服务信息
     */
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get("Host") + "/ws";
        return "ws://" + location;
    }

    //连接异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("出现异常", cause);
        ctx.channel().close();
    }

    /**
     * 接收握手请求，并响应
     * 唯一的一次http请求，用于创建websocket
     *
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        String currentIP = addr.getAddress().getHostAddress();
        String channelId = ctx.channel().id().asLongText();

        String uri = req.uri();
        //设备请求建连
        System.out.println(currentIP + "接收握手请求，url=" + uri);
        JSONObject object = new JSONObject();

        // Http解码失败，向服务器指定传输的协议为Upgrade：websocket
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            System.out.println("不是建立连接的请求");
            return;
        } else if (LAPI_REGISTER.equals(uri)) {
            object.putOnce("Nonce", getCnonce());
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer(Objects.requireNonNull(JsonUtils.toJsonString(object)), CharsetUtil.UTF_8));
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            sendHttpResponse(ctx, req, fullHttpResponse);
            return;
        } else if (uri.contains(LAPI_REGISTER + "?Vendor")) {
            System.out.println(currentIP + "设备发起第二次注册");
            // 获取请求参数
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> parameters = decoder.parameters();
            String Vendor = parameters.get("Vendor").get(0);
            String DeviceType = parameters.get("DeviceType").get(0);
            String Devicecode = parameters.get("DeviceCode").get(0);
            String Algorithm = parameters.get("Algorithm").get(0);
            String Nonce = parameters.get("Nonce").get(0);
            String Cnonce = parameters.containsKey("Cnonce") ? parameters.get("Cnonce").get(0) : "";
            String Sign = parameters.get("Sign").get(0);
            String decodedUrl = URLDecoder.decode(Sign, StandardCharsets.UTF_8);
            decodedUrl = decodedUrl.replace(" ", "+");
            System.out.println("认证签名:" + decodedUrl);
            String pstr = Vendor + "/" + DeviceType + "/" + Devicecode + "/" + Algorithm + "/" + Nonce;
            //生成服务器端的签名
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            String secret = DataPermissionHelper.ignore(() -> nvrService.checkNvrInfo(Devicecode));
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(pstr.getBytes(StandardCharsets.UTF_8));
            String encodeStr = Base64.encode(hash);
            if (!encodeStr.equals(decodedUrl)) {
                log.error("认证失败,设备ID：{}，参数：{}", Devicecode, encodeStr);
                object.putOnce("Nonce", getCnonce());
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED,
                    Unpooled.copiedBuffer(Objects.requireNonNull(JsonUtils.toJsonString(object)), CharsetUtil.UTF_8));
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
                sendHttpResponse(ctx, req, fullHttpResponse);
                return;
            } else {
                log.info("认证成功,设备ID：{}", Devicecode);
                object.putOnce("Cnonce", Cnonce);
                object.putOnce("Resign", encodeStr);
                RedisUtils.setCacheObject(CacheNames.NVR_ID_IP + Devicecode, channelId, Duration.ofMinutes(2));
                RedisUtils.setCacheObject(CacheNames.NVR_IP_ID + channelId, Devicecode, Duration.ofMinutes(5));
            }
        }

        // 握手相应处理,创建websocket握手的工厂类，本机测试
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false, 65535 * 100);
        // 根据工厂类和HTTP请求创建握手类
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            // 不支持websocket
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 通过它构造握手响应消息返回给客户端
            ChannelFuture future = handshaker.handshake(ctx.channel(), req);
            if (future.isSuccess()) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtils.toJsonString(object)));
            }
        }
    }

    /**
     * 接收WebSocket请求
     *
     */
    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame req) {
        String channelId = ctx.channel().id().asLongText();
        //接收websocket请求，格式转化
        String jsonObject = ((TextWebSocketFrame) req).text();
        WebsocketReq websocketReq = JsonUtils.parseObject(jsonObject, WebsocketReq.class);

        if (Objects.equals(LAPI_KEEPALIVE, websocketReq.getRequestURL())) {
            //用线程接受保活信息
            KeepLiveThread keepLiveThread = new KeepLiveThread(ctx.channel(), jsonObject);
            KeepLiveThreadPoolExecutor.EXECUTOR_SERVICE.execute(keepLiveThread);
            String deviceCode = RedisUtils.getCacheObject(CacheNames.NVR_IP_ID + channelId);
            if (StringUtils.isNotEmpty(deviceCode)) {
                RedisUtils.setCacheObject(CacheNames.NVR_ID_IP + deviceCode, channelId, Duration.ofMinutes(2));
                RedisUtils.setCacheObject(CacheNames.NVR_IP_ID + channelId, deviceCode, Duration.ofMinutes(2));
            } else {
                log.error("设备信息丢失重新请求，IP:{}", channelId);
                WebsocketReq infoReq = new WebsocketReq();
                websocketReq.setRequestURL("/LAPI/V1.0/System/DeviceInfo");
                websocketReq.setMethod("GET");
                websocketReq.setData(null);
                NettyEmitterManager.sendMessage(channelId, infoReq);
            }
        } else if (LAPI_UNREGISTER.equals(websocketReq.getRequestURL())) {
            System.out.println(channelId + "设备断开连接");
        } else {

            if (StringUtils.isBlank(websocketReq.getRequestURL())) {
                try {
                    log.info("收到的响应：{}", jsonObject);
                    WebsocketRsp websocketRsp = JsonUtils.parseObject(jsonObject, WebsocketRsp.class);
                    WSOperator.OnReceiveResponse(websocketRsp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // BAD_REQUEST(400) 客户端请求错误返回的应答消息
        if (res.status().code() != 200) {
            // 将返回的状态码放入缓存中，Unpooled没有使用缓存池
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
//            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // 发送应答消息
        ChannelFuture cf = ctx.channel().writeAndFlush(res);
        // 非法连接直接关闭连接
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
    }

    //计算cnonce值，cnonce用于鉴权
    public static String getCnonce() {
        double d = Math.random();
        double d1 = (double) new Date().getTime() / 1000;
        double x = d * d1;
        DecimalFormat df = new DecimalFormat("#");//四舍五入取整
        return df.format(x);
    }
}
