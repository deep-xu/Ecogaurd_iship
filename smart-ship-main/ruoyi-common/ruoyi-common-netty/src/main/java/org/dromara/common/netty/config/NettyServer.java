package org.dromara.common.netty.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.service.NvrService;
import org.dromara.common.netty.handler.WebSocketHandler;
import org.dromara.common.netty.listener.NettyTopicListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * Netty服务
 *
 **/
@Slf4j
@Component
@ConditionalOnProperty(value = "netty.enabled", havingValue = "true")
@EnableConfigurationProperties(NettyProperties.class)
public class NettyServer {

    @Bean
    public NettyTopicListener nettyTopicListener() {
        return new NettyTopicListener();
    }

    private final NettyProperties properties;
    private final NvrService nvrService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServer(NettyProperties properties, NvrService nvrService) {
        this.properties = properties;
        this.nvrService = nvrService;
    }

    @PostConstruct
    public void start() throws Exception {
        if (!properties.getEnabled()) {
            log.error("Netty server is disabled by configuration");
            return;
        }
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(properties.getWorkerThreads());
        // 服务端启动辅助类，用于设置TCP相关参数
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置为主从线程模型
        bootstrap.group(bossGroup, workerGroup)
            //设置服务端NIO通信模型
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast("http-codec", new HttpServerCodec());//设置解码器，将请求和应答消息编码或者解码消息为HTTP消息。
                    ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65535));//设置单次请求文件大小，把多个消息转换为单一的http请求或响应
                    ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());//用于大数据的分区传输，//用于向客户端发送HTML5文件，用于支持游览器和服务端进行WebSocket通信
//                    ch.pipeline().addLast("adapter",new FunWebSocketServerHandler());  //前置拦截器
                    ch.pipeline().addLast("handler", new WebSocketHandler(nvrService));//自定义业务的handler
                }
            })
            .option(ChannelOption.SO_BACKLOG, 5)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
        serverChannel = future.channel();
        System.out.println("Netty server started : " + serverChannel);
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        System.out.println("netty server stopped");
    }
}
