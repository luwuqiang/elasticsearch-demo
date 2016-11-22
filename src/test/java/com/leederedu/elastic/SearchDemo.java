package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class SearchDemo {

    public static ObjectMapper objectMapper = null;
    public static String INDEX = "leederedu";
    public static String INDEX_2 = "leederedu_2";
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
    public void search() {
        SearchResponse response = client.prepareSearch(INDEX, INDEX_2)
                .setTypes(Info.class.getSimpleName(), "type2")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("multi", "test"))                 // Query
                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();


    }

    public void searchAll() {

        // MatchAll on the whole cluster with all default options
        SearchResponse response = client.prepareSearch().execute().actionGet();

//        response.
    }

}
