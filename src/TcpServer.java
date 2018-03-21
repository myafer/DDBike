


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 123.57.210.104:8898

public class TcpServer
{
    private static final Logger logger = Logger.getLogger(TcpServer.class);
//    private static final String IP = "172.24.119.202";
    private static final String IP = "127.0.0.1";
    private static final int PORT = 8806;
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected static final int BIZTHREADSIZE = 100;
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(100);
    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();


    protected static void run()
    throws Exception
  {
     ServerBootstrap b = new ServerBootstrap();
     b.group(bossGroup, workerGroup);
     b.channel(NioServerSocketChannel.class);

     b.childHandler(new ChannelInitializer<SocketChannel>()
    {
        public void initChannel(SocketChannel ch)
        throws Exception
      {
         ChannelPipeline pipeline = ch.pipeline();
//         pipeline.addLast("decoder", new StringDecoder());
//         pipeline.addLast("encoder", new StringEncoder());
          //过滤编码
          pipeline.addLast("decoder", new ByteArrayDecoder());
          //过滤编码
          pipeline.addLast("encoder", new ByteArrayEncoder());
         pipeline.addLast(new ChannelHandler[] { new TcpServerHandler() });
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
  }



    public static Map<String, Channel> getMap() {
        return map;
    }

    public static void setMap(Map<String, Channel> map) {
        TcpServer.map = map;
    }
}