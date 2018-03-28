
import io.netty.channel.*;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import sun.jvm.hotspot.runtime.Bytes;

import java.util.ArrayList;


public class TcpServerHandler
  extends SimpleChannelInboundHandler<byte[]>
{
    private static final Logger logger = Logger.getLogger(TcpServerHandler.class);

    int imei_length = 18;

    int light =                 0x01; // 灯光控制电源开关
    int door =                  0x06; // 门禁控制电源开关
    int airCleanMachine =       0x07; // 空气净化器控制电源开关
    int bodyTester =            0x08; // 体质检测仪控制电源开关
    int airConditioner =        0x09; // 空调控制电源开关

    int switch_on = 0xff;
    int switch_off = 0x00;

    // 1 打开门 打开灯光  2 打开体质检测器 打开空调 打开空气净化器 开始计费 3 关闭体质检测器 关闭空调 关闭空气净化器 结束计费


    public byte[] openDevice(int device, int on) {
        byte[] by = {(byte)device, (byte)0x0f, (byte)0x00, (byte)0x00, (byte)on, (byte)0x00};
        byte[] crc16 = CRC16M.getSendBuf(CRC16M.getBufHexStr(by));
        return crc16;
    }

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
        // 1 打开门 打开灯光  2 打开体质检测器 打开空调 打开空气净化器 开始计费 3 关闭体质检测器 关闭空调 关闭空气净化器 结束计费
        String type = json.get("type").toString();
        // imei 号
        String imei = json.get("imei").toString();

        // 保存长连接到map
        TcpServer.getServermap().put(imei, ctx.channel());
        logger.info(TcpServer.getServermap());
        Channel ch = TcpServer.getMap().get(imei);
        if (ch != null && ch.isActive()) {

            if (type.equals("1")) {
                // 开灯
                byte[] co = openDevice(light, switch_on);
                ch.writeAndFlush(co);

                // 开门
                byte[] co1 = openDevice(door, switch_on);
                ch.writeAndFlush(co1);
            } else if (type.equals("2")) {
                // 开空气净化器
                byte[] co = openDevice(airCleanMachine, switch_on);
                ch.writeAndFlush(co);

                // 开体质检测器
                byte[] co1 = openDevice(bodyTester, switch_on);
                ch.writeAndFlush(co1);
                // 开空调
                byte[] co2 = openDevice(airConditioner, switch_on);
                ch.writeAndFlush(co2);
            } else if (type.equals("3")) {
                // 关闭命令

                // 关闭气净化器
                byte[] coo = openDevice(airCleanMachine, switch_off);
                ch.writeAndFlush(coo);
                // 关闭质检测器
                byte[] co1 = openDevice(bodyTester, switch_off);
                ch.writeAndFlush(co1);

                // 关闭空调
                byte[] co2 = openDevice(airConditioner, switch_off);

                ch.writeAndFlush(co2);
            } else {

            }


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
        } else  {  // 返回包

            if (msg.length > 17) {
                byte[] imei = new byte[17];
                byte[] co = new byte[msg.length - 17];
                for (int j = 0; j < imei.length; j++) {
                    imei[j] = msg[j];
                }
                for (int i = imei.length; i < msg.length; i++) {
                    co[i - imei.length] = msg[i];
                }
                logger.info("获取长连接" + bytesToHexString(imei));

                // 返回结果通知服务器
                TcpServer.getServermap().get("5a" + bytesToHexString(imei) + "a5").writeAndFlush("{\"status\": \"0\", \"message\": \"成功！\"}");

                // 通知硬件接收成功
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


