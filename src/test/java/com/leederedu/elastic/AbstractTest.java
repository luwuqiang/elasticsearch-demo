package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.UnknownHostException;

/**
 * Created by liuwuqiang on 2016/11/30.
 */
public class AbstractTest {

    public static ObjectMapper objectMapper = null;
    public static String INDEX = "leederedu";
    public static Client client;
    public static String clusterAddresses = "192.168.135.134:9300,192.168.135.134:9302,192.168.135.134:9303";
//        public static String clusterAddresses = "192.168.135.1:9300";

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        objectMapper = new ObjectMapper();
        ESClient.setClusterAddresses(AbstractTest.clusterAddresses);
        ESClient.initializeSettings();
        client = ESClient.getClient();
    }

    @AfterClass
    public static void afterClass() {
        ESClient.closeTransportClient();
    }


}
