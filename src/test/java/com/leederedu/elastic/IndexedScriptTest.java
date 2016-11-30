package com.leederedu.elastic;

import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.junit.Test;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class IndexedScriptTest extends AbstractTest {

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
