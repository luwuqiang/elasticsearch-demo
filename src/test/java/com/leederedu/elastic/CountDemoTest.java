package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class CountDemoTest {

    public static ObjectMapper objectMapper = null;
    public static String INDEX = "leederedu";
    public static String INDEX_2 = "test_index";
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
    public void count() {
        CountResponse response = client.prepareCount(INDEX)
                .setQuery(termQuery("_type", "type1"))
                .execute()
                .actionGet();

        System.out.println(response.getCount());
    }
}
