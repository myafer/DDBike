
import io.netty.channel.*;

import java.io.PrintStream;

import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptObject;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import sun.plugin.javascript.navig.JSType;

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

    int imei_length = 18;

    int light =     0x01; // 灯光控制电源开关
    int door =      0x06; // 门禁控制电源开关
    int air =       0x07; // 空气净化器控制电源开关
    int body =      0x08; // 体质检测仪控制电源开关
    int kongtiao =  0x09; // 空调控制电源开关

    // 控制硬件编码

    public String controlEecode(String type, String device, String on) {

        return "";
    }

    // 控制硬件解码
    public String controlDecode(String hexstr) {

        return "";
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //删除Channel Map中的失效Client

        TcpServer.getMap().remove(getIPString(ctx));
        ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);

        JSONObject json = JSONObject.fromObject(msg.toString());
        // 1 单个开关控制  2全部开关控制
        String type = json.get("type").toString();
        // imei 号
        String imei = json.get("imei").toString();
        // 单个开关地址 1 灯光控制电源开关 2 门禁控制电源开关 3  空气净化器控制电源开关 4  体质检测仪控制电源开关 5  空调控制电源开关      全部开关默认为0
        String switch_num = json.get("switch_num").toString();
        // 是否开启 1 开 0 关
        String on = json.get("on").toString();

        // 保存长连接到map
        TcpServer.getServermap().put(imei, ctx.channel());
        logger.info(TcpServer.getServermap());
        Channel ch = TcpServer.getMap().get(imei);
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(hexStringToBytes("FE 0F 00 00 00 04 01 FF 31 D2"));
        } else {
            logger.info("没有找到相应的线程或者设备失活。。。应该是设备不在线。。设备号：" + imei);
            ctx.channel().writeAndFlush("{\"status\": \"0\", \"message\": \"机器不在线，请联系客服人员。\"}");
            ctx.channel().close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        logger.info(bytesToHexString(msg));
        for (int i = 0; i < msg.length; i++ ) {
            System.out.print(msg[i]);
            System.out.print(" ");
        }
        // 心跳
        if (msg[0] == 90 && msg[msg.length-1] == -91) {
            logger.info("心跳数据进入。。。");
            // 根据心跳保存长连接
            TcpServer.getMap().put(bytesToHexString(msg), ctx.channel());
            // 返回心跳数据
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

//  public static DataType getType(String source) {
//      if (source.contains("ping")) {
//          return DataType.PING;
//      } else if (source.contains("login")) {
//          return DataType.LOGIN;
//      } else if (source.contains("post")) {
//          return DataType.POST;
//      } else {
//          return DataType.NONE;
//      }
//  }

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


