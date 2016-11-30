package com.leederedu.elastic;

import org.elasticsearch.action.count.CountResponse;
import org.junit.Test;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class CountDemoTest extends AbstractTest{

    public static String INDEX_2 = "test_index";

    @Test
    public void count() {
        CountResponse response = client.prepareCount(INDEX)
                .setQuery(termQuery("_type", "type1"))
                .execute()
                .actionGet();

        System.out.println(response.getCount());
    }
}
