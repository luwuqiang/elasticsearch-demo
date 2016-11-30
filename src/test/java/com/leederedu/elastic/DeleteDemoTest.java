package com.leederedu.elastic;

import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.delete.DeleteResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class DeleteDemoTest extends AbstractTest {

    /**
     * 通过文档ID来删除文档
     */
    @Test
    public void deleteById() {

        String id = "1001";
        DeleteResponse response = ESClient.getClient().prepareDelete(INDEX, Info.class.getSimpleName(), id)
                .get();

        assertEquals(false, response.isFound());
    }

    /**
     * 通过脚本删除文档
     */
    @Test
    public void deleteByQuery() {
//        QueryBuilder query = matchQuery("title","title");
//        ESClient.getClient().prepare

    }

}
