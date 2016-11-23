package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class IndexedScriptTest {
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
    public void indexedScript() {
        PutIndexedScriptResponse putResponse = client.preparePutIndexedScript()
                .setScriptLang("groovy")
                .setId("script1")
                .setSource("script", "_score * doc['my_numeric_field'].value")
                .execute()
                .actionGet();

        GetIndexedScriptResponse getResponse = client.prepareGetIndexedScript()
                .setScriptLang("groovy")
                .setId("script1")
                .execute()
                .actionGet();

        DeleteIndexedScriptResponse delResponse = client.prepareDeleteIndexedScript()
                .setScriptLang("groovy")
                .setId("script1")
                .execute()
                .actionGet();
    }
}
