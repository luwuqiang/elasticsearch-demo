package com.leederedu.elastic;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * 索引数据
 * Created by liuwuqiang on 2016/11/22.
 */
public class IndexDemoTest {

    public static ObjectMapper objectMapper = null;
    public static String INDEX = "leederedu";
    private static Client client;

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        objectMapper = new ObjectMapper();
        ESClient.setClusterAddresses(ESClientTest.clusterAddresses);
        ESClient.initializeSettings();
        client = ESClient.getClient();
    }

    @AfterClass
    public static void afterClass() {
        ESClient.closeTransportClient();
    }

    @Test
    public void indexTest() throws JsonProcessingException {
//        Info info = new Info().setId(1102).setName("Hello, World").setContext("珠江新城").setSortNum(3001).setUrl("http://192.168.19.126");
        Info info = new Info().setId(1103).setName("节日").setContext("广州国际灯光节").setSortNum(3001).setUrl("http://192.168.19.126");
        IndexResponse response = client
                .prepareIndex(INDEX, info.getClass().getSimpleName(), info.getId() + "")
                .setSource(objectMapper.writeValueAsBytes(info))
                .execute().actionGet();

        System.out.println(response.toString());
    }

    @Test
    public void indexTest2() throws IOException {
        //es索引数据非常方便，只需构建个json格式的数据提交到es就行
        XContentBuilder doc = jsonBuilder()
                .startObject()
                .field("title", "this is a title!")
                .field("description", "descript what?")
                .field("price", 100)
                .field("onSale", true)
                .field("type", 1)
                .field("createDate", new Date())
                .endObject();
        IndexResponse response =  client.prepareIndex(INDEX, "product")
                .setId("1") //需要指定id，否则会生成一个随机序列号
                .setSource(doc).execute().actionGet();
        System.out.println(response.toString());
    }
}