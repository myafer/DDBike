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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by afer on 2018/4/6.
 */
public class AsynHttp {

    private static final Logger logger = Logger.getLogger(AsynHttp.class);
    public static String url = "http://192.168.18.27:8038/";

    public static void sendDeviceStatus(String fdevice, String fswitchResult, String fimei) {
        logger.info(String.format("device = %s, result = %s, fimei = %s", fdevice, fswitchResult, fimei));
        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info(String.format("device = %s, result = %s, fimei = %s", fdevice, fswitchResult, fimei));
                String result = HttpRequest.sendPost (url + "/Equipment/UpdateControlStatus", "Code="+ fimei +"&Number=" + fdevice + "&Status=" + fswitchResult);
                logger.info(result);
            }
        }).start();
    }

    public static void main(final String[] args) throws Exception {
        logger.info("2222222222");
        sendDeviceStatus("1", "2", "2");
        logger.info("cccccccc");
    }

}
