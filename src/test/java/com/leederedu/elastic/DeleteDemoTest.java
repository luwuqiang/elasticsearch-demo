package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.delete.DeleteResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class DeleteDemoTest {

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
    public void delete() {
        String id = "1001";
        DeleteResponse response = ESClient.getClient().prepareDelete(INDEX, Info.class.getSimpleName(), id)
                .get();

        assertEquals(false, response.isFound());
    }

}
