package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class UpdateDemoTest {

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
    public void update() throws IOException, ExecutionException, InterruptedException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(INDEX);
        updateRequest.type(Info.class.getSimpleName());
        updateRequest.id("1001");

        updateRequest.doc(jsonBuilder()
                .startObject()
                .field("name2", "测试")
                .endObject());
        ESClient.getClient().update(updateRequest).get();

        //域(field) 不存在时会新增一个新域
    }

    @Test
    public void usePrepareUpdate() throws IOException, ExecutionException, InterruptedException {

//        ESClient.getClient().prepareUpdate(INDEX, Info.class.getSimpleName(), "1001")
//                .setScript(new Script("ctx._source.gender = \"male\""  , ScriptService.ScriptType.INLINE, null, null))
//                .get();

        ESClient.getClient().prepareUpdate(INDEX, Info.class.getSimpleName(), "1001")
                .setDoc(jsonBuilder()
                        .startObject()
                        .field("name", "male")
                        .endObject())
                .get();
    }

    @Test
    public void updateByScript() throws ExecutionException, InterruptedException {
        // TODO: 2016/11/22
//        UpdateRequest updateRequest = new UpdateRequest("ttl", "doc", "1")
//                .script(new Script("ctx._source.gender = \"male\""));
//        ESClient.getClient().update(updateRequest).get();
    }

    /**
     * 如果文档不存在将新建，但不更新字段
     */
    @Test
    public void updateWithUpsert() throws IOException, ExecutionException, InterruptedException {
        Info info = new Info().setId(1002).setName("测试").setContext("测试文章内容").setSortNum(2000).setUrl("http://192.168.10.124");
        IndexRequest indexRequest = new IndexRequest(INDEX, Info.class.getSimpleName(), info.getId() + "")
                .source(objectMapper.writeValueAsBytes(info));

        UpdateRequest updateRequest = new UpdateRequest(INDEX, Info.class.getSimpleName(), info.getId() + "")
                .doc(jsonBuilder()
                        .startObject()
                        .field("name", "测试updateWithUpsert")
                        .endObject())
                .upsert(indexRequest);
        ESClient.getClient().update(updateRequest).get();
    }


}
