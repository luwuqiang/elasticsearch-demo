package com.leederedu.elastic;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class IndexDemoTest {

    public static ObjectMapper objectMapper = null;
    public static String INDEX = "leederedu";
    private static Client client;

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        objectMapper = new ObjectMapper();
        ESClient.initializeSettings();
        client = ESClient.getClient();
    }

    @AfterClass
    public static void afterClass() {
        ESClient.closeTransportClient();
    }

    @Test
    public void IndexResponseTest() throws JsonProcessingException {
//        Info info = new Info().setId(1102).setName("Hello, World").setContext("珠江新城").setSortNum(3001).setUrl("http://192.168.19.126");
        Info info = new Info().setId(1103).setName("节日").setContext("广州国际灯光节").setSortNum(3001).setUrl("http://192.168.19.126");
        IndexResponse response = client
                .prepareIndex(INDEX, info.getClass().getSimpleName(), info.getId() + "")
                .setSource(objectMapper.writeValueAsBytes(info))
                .get();

        System.out.println(response.toString());
    }
}