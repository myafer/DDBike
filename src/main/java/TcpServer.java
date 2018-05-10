


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 123.57.210.104:8898

public class TcpServer
{
//    private static final Logger logger = Logger.getLogger(TcpServer.class);
    private static final Log logger =  LogFactory.getLog("TcpServer");

    private static final String IP = "172.24.119.202";
//    private static final String IP = "192.168.18.133";
    private static final int PORT = 8806;
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected static final int BIZTHREADSIZE = 100;
//    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
//    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(100);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(2);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(2);

    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();
    private static Map<String, Channel> servermap = new ConcurrentHashMap<String, Channel>();
    private static Map<String, Thread> lightThreadMap = new ConcurrentHashMap<String, Thread>();

    protected static void run()
    throws Exception
  {
     ServerBootstrap b = new ServerBootstrap();
     b.group(bossGroup, workerGroup);
     b.channel(NioServerSocketChannel.class);
     b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
     b.childOption(ChannelOption.TCP_NODELAY, true);
     b.childHandler(new ChannelInitializer<SocketChannel>()
    {
        public void initChannel(SocketChannel ch)
        throws Exception
      {
         ChannelPipeline pipeline = ch.pipeline();
//          pipeline.addLast("deecoder", new StringDecoder());
//          pipeline.addLast("enncoder", new StringEncoder());
          //过滤编码
          pipeline.addLast("decoder", new ByteArrayDecoder());
          //过滤编码
          pipeline.addLast("encoder", new ByteArrayEncoder());
          pipeline.addLast(new TcpServerHandler());
      }
     });
     b.bind(IP, 8806).sync();
  }
  protected static void shutdown()
  {
     workerGroup.shutdownGracefully();
     bossGroup.shutdownGracefully();
  }
  public static void main(String[] args)
    throws Exception
  {
     run();
     logger.info("TCP服务开启...xxx");
     System.out.println("TCP服务开启...xxx");
  }

    public static Map<String, Channel> getMap() {
        return map;
    }

    public static void setMap(Map<String, Channel> map) {
        TcpServer.map = map;
    }

    public static Map<String, Channel> getServermap() {
        return servermap;
    }

    public static void setServermap(Map<String, Channel> map) {
        TcpServer.servermap = map;
    }

    public static Map<String, Thread> getLightThreadMap() {
        return lightThreadMap;
    }

    public static void setLightThreadMap(Map<String, Thread> map) {
        TcpServer.lightThreadMap = map;
    }

}