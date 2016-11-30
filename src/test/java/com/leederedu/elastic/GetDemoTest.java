package com.leederedu.elastic;

import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.junit.Test;

import java.io.IOException;

/**
 * 搜索数据
 * Created by liuwuqiang on 2016/11/22.
 */
public class GetDemoTest extends AbstractTest {

    @Test
    public void getDataTest() throws IOException {
        GetResponse response = ESClient.getClient()
                .prepareGet(INDEX, Info.class.getSimpleName(), "1003")
                .setOperationThreaded(false)
                .get();

        if (response.getSourceAsBytes() != null) {
//            Info info = objectMapper.readValue(response.getSourceAsBytes(), Info.class);
//            System.out.println(info.toString());
            System.out.println(response.getSourceAsString());
        }
    }

    @Test
    public void multiGetTest() {
        String[] ids = new String[]{"1001", "1002", "1003", "1004"};
        MultiGetResponse response = ESClient.getClient().prepareMultiGet()
                .add(INDEX, Info.class.getSimpleName(), ids).get();

        for (MultiGetItemResponse itemResponse : response) {
            GetResponse resp = itemResponse.getResponse();
            if (resp.isExists()) {
                String json = resp.getSourceAsString();
                System.out.println(json);
            }
        }

    }

}
