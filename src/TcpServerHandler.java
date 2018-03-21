
import io.netty.channel.*;

import java.io.PrintStream;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public class TcpServerHandler
  extends SimpleChannelInboundHandler<byte[]>
{
    private static final Logger logger = Logger.getLogger(TcpServerHandler.class);

    enum DataType {
        NONE,
        PING,
        LOGIN,
        POST,
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //删除Channel Map中的失效Client
        TCPServerNetty.getMap().remove(getIPString(ctx));
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

//        TcpServer.getMap().put(String.valueOf(msg), ctx.channel());
        logger.info(bytesToHexString(msg));

        for (int i = 0; i < msg.length; i++ ) {
            System.out.print(msg[i]);
            System.out.print(" ");
        }
        System.out.print("--------");
        logger.info(msg[0]);
        logger.info(msg[0] == 0x5a);
        logger.info(msg[msg.length-1]);
        logger.info(msg[msg.length-1] == 0xa5);
        if (msg[0] == 90 && msg[msg.length-1] == -91) {
            logger.info("进入了。。。。。");
            TcpServer.getMap().put(bytesToHexString(msg), ctx.channel());
            ctx.writeAndFlush(msg);
        } else  {
            byte[] imei = new byte[17];
            byte[] co = new byte[msg.length - 17];
            for (int j = 0; j < imei.length; j++) {
                imei[j] = msg[j];
            }
            for (int i = imei.length; i < msg.length; i++) {
                co[i - imei.length] = msg[i];
            }
            logger.info("获取长连接" + bytesToHexString(imei));
            try {
                logger.info("获取长连接");
                TcpServer.getMap().get(bytesToHexString(imei)).writeAndFlush(co);
            } catch (Exception e) {
                logger.info("失败了");
                ctx.writeAndFlush(msg);
            }
        }
    }

    //    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
//    throws Exception
//  {
//      TcpServer.getMap().put(msg.toString(), ctx.channel());
//
//      logger.info(ctx.channel().id().asLongText());
//      logger.info("SERVER接收到消息:" + msg);
//      logger.info(msg.toString() == "haha");
//      if (msg.toString().contains("haha")) {
//          logger.info("122222222222");
//          logger.info(TcpServer.getMap());
//          logger.info("oooo   " + msg.toString().replace("haha", ""));
//          ctx.channel().writeAndFlush("zf|success\n");
//          TcpServer.getMap().get(msg.toString().replace("haha", "")).writeAndFlush("oooooooooo\n").addListener(new ChannelFutureListener(){
//              @Override
//              public void operationComplete(ChannelFuture future)
//                      throws Exception {
//                  logger.info("下发成功！");
//              }
//          });;
//      } else {
//          ctx.channel().writeAndFlush("gt|success\n");
//      }
//  }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public  String printHexString( byte[] b) {
        String a = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a = a+hex;
        }
        return a;
    }


  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    throws Exception
  {
     logger.warn("Unexpected exception from downstream.", cause);
     ctx.close();
  }

  public static DataType getType(String source) {
      if (source.contains("ping")) {
          return DataType.PING;
      } else if (source.contains("login")) {
          return DataType.LOGIN;
      } else if (source.contains("post")) {
          return DataType.POST;
      } else {
          return DataType.NONE;
      }
  }

  public static boolean trimFirstAndLastChar(String source)
  {
     if (((source == null ? 1 : 0) | (source.length() == 0 ? 1 : 0)) != 0) {
       return false;
    }
     char begin = source.charAt(0);
     char end = source.charAt(source.length() - 1);
     if ((begin == '*') && (end == '&')) {
       return true;
    }
     return false;
  }

  public static String cutString(String source)
  {
     String str = source.substring(1, source.length() - 1);
     System.out.println(str);
     return str;
  }

    public static String getIPString(ChannelHandlerContext ctx) {
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        logger.info(socketString);
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, socketString.length());
        return ipString;
    }
}


