import org.apache.http.*;
//import org.apache.http.HttpRequest;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by afer on 2018/4/6.
 */
public class AsynHttp {

    private static final Logger logger = Logger.getLogger(AsynHttp.class);
    public static String url = "http://panda-play.com/index.php/api";

    public static void sendDeviceStatus(String fdevice, String fswitchResult, String fimei) {
        logger.info(String.format("device = %s, result = %s, fimei = %s", fdevice, fswitchResult, fimei));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tt = fimei.replaceAll("\\s", "20%");
                logger.info(String.format("device = %s, result = %s, fimei = %s", fdevice, fswitchResult, tt));
                String result = HttpRequest.sendGet (url + "/Equipment/UpdateControlStatus", "Code="+ tt +"&Number=" + fdevice + "&Status=" + fswitchResult);
                logger.info(result);
                logger.info("Code="+ tt +"&Number=" + fdevice + "&Status=" + fswitchResult);
            }
        }).start();
    }

    public static void main(final String[] args) throws Exception {
        logger.info("2222222222");
        sendDeviceStatus("1", "2", "5a 3 5 8 5 1 1 0 4 0 3 0 7 9 7 1 a5");
        logger.info("cccccccc");
    }

}
