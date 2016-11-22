package com.leederedu.elastic;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.index.IndexResponse;
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

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        objectMapper = new ObjectMapper();
        ESClient.initializeSettings();
    }

    @AfterClass
    public static void afterClass() {
        ESClient.closeTransportClient();
    }

    @Test
    public void IndexResponseTest() throws JsonProcessingException {
        Info info = new Info().setId(1001).setName("测试").setContext("测试文章内容").setSortNum(2000).setUrl("http://192.168.10.124");

        IndexResponse response = ESClient.getClient()
                .prepareIndex(INDEX, info.getClass().getSimpleName(), info.getId() + "")
                .setSource(objectMapper.writeValueAsBytes(info))
                .get();

        System.out.println(response.toString());
    }
}