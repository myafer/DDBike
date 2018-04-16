
import io.netty.channel.*;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;



public class TcpServerHandler
  extends SimpleChannelInboundHandler<byte[]>
{
    private static final Log logger =  LogFactory.getLog("TcpServerHandler");

    static int imei_length = 18;

    static int ii = 0x02;
    static int light =                 0x01; // 灯光控制电源开关
    static int door =                  0x06; // 门禁控制电源开关
    static int airCleanMachine =       0x07; // 空气净化器控制电源开关
    static int bodyTester =            0x08; // 体质检测仪控制电源开关
    static int airConditioner =        0x09; // 空调控制电源开关

    static int switch_on = 0xff;
    static int switch_off = 0x00;

    String url = "http://192.168.18.27:8038/";

    // 1 打开门 打开灯光  2 打开体质检测器 打开空调 打开空气净化器 开始计费 3 关闭体质检测器 关闭空调 关闭空气净化器 结束计费


    public static byte[] openDevice(int device, int on) {
        byte[] by = {(byte)device, (byte)0x05, (byte)0x00, (byte)0x00, (byte)on, (byte)0x00};
        byte[] crc16 = CRC16M.getSendBuf(CRC16M.getBufHexStr(by));
        return crc16;
    }

    public byte[] checkDevice(int device) {
        byte[] by = {(byte)device, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04};
        byte[] crc16 = CRC16M.getSendBuf(CRC16M.getBufHexStr(by));
        return crc16;
    }

    // 控制硬件解码
    public void controlDecode(byte[] imei, byte[] data) {
        String device = "";
        if (data[0] == 0x01) {
            device = "0"; // 灯
        } else if (data[0] == 0x06) {
            device = "1"; // 门
        } else if (data[0] == 0x07) {
            device = "2"; // 空气净化器
        } else if (data[0] == 0x08) {
            device = "3"; // 体质检测仪
        } else if (data[0] == 0x09) {
            device = "4"; // 空调
        }

        // 0x0f 控制  0x01 查询
        String switchResult = "";
        logger.info("------- 返回结果 --------");
        logger.info(bytesToHexString(data));
        int type = (int)(data[1] & 0xFF);
        if (type == 0x05) {
            if ((int)(data[4] & 0xFF) == switch_on) {
                switchResult = "1";
            } else  {
                switchResult = "0";
            }
        } else if (type == 0x01) { // 查询结果返回
            if ((int)(data[3] & 0xFF) > 0) {
                switchResult = "1";
            } else  {
                switchResult = "0";
            }
        }
        // 返回命令检测
        logger.info("device: " + device + "  switch " + switchResult);
        // 上传结果到服务器
        if (!device.equals("") && !switchResult.equals("")) {
            AsynHttp.sendDeviceStatus(device, switchResult, bytesToHexString(imei));
        }
    }

    public void checkStatus(Channel ch, int device) {
        byte[] co1 = checkDevice(device);
        ch.writeAndFlush(co1);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

//        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//        for (Integer key : map.keySet()) {
//            Integer value = map.get(key);
//            System.out.println("Key = " + key + ", Value = " + value);

        for (String key: TcpServer.getMap().keySet()) {
            Channel ch = TcpServer.getMap().get(key);
            if (ch == null || ch.id() == ctx.channel().id()) {
                TcpServer.getMap().remove(key);
                break;
            }
        }

        for (String key: TcpServer.getServermap().keySet()) {
            Channel ch = TcpServer.getServermap().get(key);
            if (ch == null || ch.id() == ctx.channel().id()) {
                TcpServer.getServermap().remove(key);
                break;
            }
        }

        ctx.close();
    }

    public boolean isJson(String content){
        try {
            JSONObject json = JSONObject.fromObject(content);
            return  true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        byte[] by = openDevice(ii, switch_on);
        logger.info(bytesToHexString(by));
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        logger.info("88888888" + msg);
        String msgs = new String(msg,"UTF-8");
        if (isJson(msgs)) {
            JSONObject json = JSONObject.fromObject(msgs);
            // 1 打开门 打开灯光  2 打开体质检测器 打开空调 打开空气净化器 开始计费 3 关闭体质检测器 关闭空调 关闭空气净化器 结束计费
            String type = json.get("type").toString();
            // imei 号
            String imei = json.get("imei").toString();
            logger.info("控制命令： " + json);

            // 保存长连接到map
//            TcpServer.getServermap().put(imei, ctx.channel());
//            logger.info(TcpServer.getServermap());
//            logger.info(TcpServer.getMap());
//            logger.info("imei " + imei);
            Channel ch = TcpServer.getMap().get(imei);
            logger.info(ch);
            if (ch != null && ch.isActive()) {
                logger.info("xxxxxxxxxxxxx");
                if (type.equals("11")) {
                    byte[] co = openDevice(light, switch_on);
                    logger.info(bytesToHexString(co));
                    logger.info("开灯！！！");
                    ch.writeAndFlush(co);
                } else if (type.equals("12")) {
                    byte[] co1 = openDevice(door, switch_on);
                    logger.info("开门！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("13")) {
                    byte[] co1 = openDevice(airCleanMachine, switch_on);
                    logger.info("开空气净化器！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("14")) {
                    byte[] co1 = openDevice(bodyTester, switch_on);
                    logger.info("开体质检测仪！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("15")) {
                    byte[] co1 = openDevice(airConditioner, switch_on);
                    logger.info("开空调！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);

                } else if (type.equals("01")) {
                    byte[] co1 = openDevice(light, switch_off);
                    logger.info("关灯！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("02")) {
                    byte[] co1 = openDevice(door, switch_off);
                    logger.info("关门！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("03")) {
                    byte[] co1 = openDevice(airCleanMachine, switch_off);
                    logger.info("关空气净化器！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("04")) {
                    byte[] co1 = openDevice(bodyTester, switch_off);
                    logger.info("关体质检测仪！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);
                } else if (type.equals("05")) {
                    byte[] co1 = openDevice(airConditioner, switch_off);
                    logger.info("关空调！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);

                } else if (type.equals("21")){
                    checkStatus(ch, light);
                } else if (type.equals("22")){
                    checkStatus(ch, door);
                } else if (type.equals("23")){
                    checkStatus(ch, airCleanMachine);
                } else if (type.equals("24")){
                    checkStatus(ch, bodyTester);
                } else if (type.equals("25")){
                    checkStatus(ch, airConditioner);
                } else if (type.equals("-1")) {
                    ch.writeAndFlush(hexStringToBytes("FE 05 00 00 FF 00 98 35"));
                } else if (type.equals("31")) {

                    byte[] co = openDevice(light, switch_on);
                    logger.info(bytesToHexString(co));
                    logger.info("开灯！！！");
                    ch.writeAndFlush(co);

                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(100);
                                byte[] co1 = openDevice(door, switch_on);
                                logger.info("开门！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(10000);
                                byte[] co1 = openDevice(door, switch_off);
                                logger.info("关门！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                } else if (type.equals("32")) {

                    byte[] co1 = openDevice(airCleanMachine, switch_on);
                    logger.info("开空气净化器！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);

                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(500);
                                byte[] co1 = openDevice(bodyTester, switch_on);
                                logger.info("开体质检测仪！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(1000);

                                byte[] co1 = openDevice(airConditioner, switch_on);
                                logger.info("开空调！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                } else if (type.equals("33")){
                    byte[] co1 = openDevice(airCleanMachine, switch_off);
                    logger.info("关空气净化器！！！");
                    logger.info(bytesToHexString(co1));
                    ch.writeAndFlush(co1);

                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(500);
                                byte[] co1 = openDevice(bodyTester, switch_off);
                                logger.info("关体质检测仪！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(1000);

                                byte[] co1 = openDevice(airConditioner, switch_off);
                                logger.info("关空调！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(30000);
                                byte[] co1 = openDevice(light, switch_off);
                                logger.info("关灯！！！");
                                logger.info(bytesToHexString(co1));
                                ch.writeAndFlush(co1);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                } else if (type.equals("30")) {
                    checkStatus(ch, light);
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(500);
                                checkStatus(ch, door);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(1000);
                                checkStatus(ch, airCleanMachine);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(1500);
                                checkStatus(ch, bodyTester);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(2000);
                                checkStatus(ch, airConditioner);
                            } catch (InterruptedException e) { }
                        }
                    }.start();
                }
            } else {
                logger.info("没有找到相应的线程或者设备失活。。。应该是设备不在线。。设备号：" + imei);
//                ctx.channel().writeAndFlush("{\"status\": \"0\", \"message\": \"机器不在线，请联系客服人员。\"}");
//                ctx.channel().close();
            }
            ctx.close();
        } else  {
            byte[] bmsg = msg;
//            for (int i = 0; i < bmsg.length; i++ ) {
//                System.out.print(bmsg[i]);
//                System.out.print(" ");
//            }
            // 心跳
            if ((int)(bmsg[0] & 0xFF) == 0x3a && (int)(bmsg[bmsg.length-1]  & 0xFF) == 0x2a) {
                logger.info("心跳数据进入。。。");
                // 根据心跳保存长连接
                TcpServer.getMap().put(bytesToHexString(bmsg), ctx.channel());
                logger.info(TcpServer.getMap());
                ctx.channel().writeAndFlush(bmsg);
            } else  {  // 返回包

                if (bmsg.length > 15) {
                    byte[] imei = new byte[15];
                    byte[] co = new byte[bmsg.length - 15];
                    for (int j = 0; j < imei.length; j++) {
                        imei[j] = bmsg[j];
                    }
                    for (int i = imei.length; i < bmsg.length; i++) {
                        co[i - imei.length] = bmsg[i];
                    }
                    logger.info(bytesToHexString(bmsg));
                    logger.info("获取长连接" + bytesToHexString(imei));
                    logger.info(TcpServer.getServermap());
                    // 解析命令 并上传到HTTP服务器
                    controlDecode(imei, co);
                }
            }
//            ctx.channel().flush();

        }
    }


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
            if (i != src.length-1) {
                stringBuilder.append(" ");
            }
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

    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    throws Exception
  {
     logger.warn("Unexpected exception from downstream.", cause);
     ctx.close();
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

//if (type.equals("1")) {
//        byte[] co = openDevice(door, switch_on);
//        logger.info(bytesToHexString(co));
//        logger.info("开门！！！");
//        ch.writeAndFlush(co);
//        try {
//        // 括号内的参数是毫秒值,线程休眠1s
//        Thread.sleep(500);
//        byte[] co1 = openDevice(light, switch_on);
//        logger.info("开灯！！！");
//        logger.info(bytesToHexString(co1));
//        ch.writeAndFlush(co1);
//        } catch (InterruptedException e) {logger.info("error");}
//        } else if (type.equals("2")) {
//        byte[] co1 = openDevice(airCleanMachine, switch_on);
//        logger.info("开空气净化器！！！");
//        logger.info(bytesToHexString(co1));
//        ch.writeAndFlush(co1);
//        try {
//        // 括号内的参数是毫秒值,线程休眠1s
//        Thread.sleep(500);
//        byte[] co2 = openDevice(bodyTester, switch_on);
//        logger.info("开体质检测仪！！！");
//        logger.info(bytesToHexString(co2));
//        ch.writeAndFlush(co2);
//        } catch (InterruptedException e) {logger.info("error");}
//        try {
//        // 括号内的参数是毫秒值,线程休眠2s
//        Thread.sleep(1000);
//        byte[] co3 = openDevice(airConditioner, switch_on);
//        logger.info("开空调！！！");
//        logger.info(bytesToHexString(co3));
//        ch.writeAndFlush(co3);
//        } catch (InterruptedException e) {logger.info("error");}
//        } else if (type.equals("3")) {
//        byte[] co1 = openDevice(airCleanMachine, switch_off);
//        logger.info("关空气净化器！！！");
//        logger.info(bytesToHexString(co1));
//        ch.writeAndFlush(co1);
//        try {
//        // 括号内的参数是毫秒值,线程休眠1s
//        Thread.sleep(500);
//        byte[] co2 = openDevice(bodyTester, switch_off);
//        logger.info("关体质检测仪！！！");
//        logger.info(bytesToHexString(co2));
//        ch.writeAndFlush(co2);
//        } catch (InterruptedException e) {logger.info("error");}
//        try {
//        // 括号内的参数是毫秒值,线程休眠2s
//        Thread.sleep(1000);
//        byte[] co3 = openDevice(airConditioner, switch_off);
//        logger.info("关空调！！！");
//        logger.info(bytesToHexString(co3));
//        ch.writeAndFlush(co3);
//        } catch (InterruptedException e) { logger.info("error"); }
//        } else if (type.equals("4")) {
//        byte[] co3 = openDevice(light, switch_off);
//        logger.info("关灯！！！");
//        logger.info(bytesToHexString(co3));
//        ch.writeAndFlush(co3);
//        } else if (type.equals("6")) {
//        byte[] co3 = openDevice(door, switch_off);
//        logger.info("关门！！！");
//        logger.info(bytesToHexString(co3));
//        ch.writeAndFlush(co3);
//        } else if (type.equals("5")) {
//        byte[] co3 = openDevice(light, switch_on);
//        logger.info("开灯！！！");
//        logger.info(bytesToHexString(co3));
//        ch.writeAndFlush(co3);
//        }  else if (type.equals("0")) {
//        checkStatus(ch, door);
//        try {
//        Thread.sleep(200);
//        checkStatus(ch, light);
//        } catch (InterruptedException e) {logger.info("error");}
//        try {
//        Thread.sleep(400);
//        checkStatus(ch, airCleanMachine);
//        } catch (InterruptedException e) {logger.info("error");}
//        try {
//        Thread.sleep(600);
//        checkStatus(ch, bodyTester);
//        } catch (InterruptedException e) {logger.info("error");}
//        try {
//        Thread.sleep(800);
//        checkStatus(ch, airConditioner);
//        } catch (InterruptedException e) {logger.info("error");}
//        } else if (type.equals("-1")) {
//        byte[] co1 = openDevice(door, switch_off);
//        logger.info("关门！！！");
//        logger.info(bytesToHexString(co1));
//        ch.writeAndFlush(co1);
//        try {
//        // 括号内的参数是毫秒值,线程休眠1s
//        Thread.sleep(500);
//        byte[] co2 = openDevice(light, switch_off);
//        logger.info("关灯！！！");
//        logger.info(bytesToHexString(co2));
//        ch.writeAndFlush(co2);
//        } catch (InterruptedException e) {logger.info("error");}
//        }
