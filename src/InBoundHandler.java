import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by afer on 2018/3/17.
 */


public class InBoundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = Logger.getLogger(InBoundHandler.class);
    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("CLIENT" + getRemoteAddress(ctx) + " 接入连接");
        //往channel map中添加channel信息
        TCPServerNetty.getMap().put(getIPString(ctx), ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //删除Channel Map中的失效Client
        TCPServerNetty.getMap().remove(getIPString(ctx));
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(msg);
        ctx.channel().writeAndFlush("gt|success\n");

        if (msg == "haha") {
            TCPServerNetty.getMap().get(getIPString(ctx)).writeAndFlush("oooooooooo\n");
        }

    }




    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        String socketString = ctx.channel().remoteAddress().toString();

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.info("Client: " + socketString + " READER_IDLE 读超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.info("Client: " + socketString + " WRITER_IDLE 写超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.ALL_IDLE) {
                logger.info("Client: " + socketString + " ALL_IDLE 总超时");
                ctx.disconnect();
            }
        }
    }

    public static String getIPString(ChannelHandlerContext ctx) {
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }


    public static String getRemoteAddress(ChannelHandlerContext ctx) {
        String socketString = "";
        socketString = ctx.channel().remoteAddress().toString();
        return socketString;
    }


    private String getKeyFromArray(byte[] addressDomain) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < 5; i++) {
            sBuffer.append(addressDomain[i]);
        }
        return sBuffer.toString();
    }

    protected String to8BitString(String binaryString) {
        int len = binaryString.length();
        for (int i = 0; i < 8 - len; i++) {
            binaryString = "0" + binaryString;
        }
        return binaryString;
    }

    protected static byte[] combine2Byte(byte[] bt1, byte[] bt2) {
        byte[] byteResult = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, byteResult, 0, bt1.length);
        System.arraycopy(bt2, 0, byteResult, bt1.length, bt2.length);
        return byteResult;
    }
}