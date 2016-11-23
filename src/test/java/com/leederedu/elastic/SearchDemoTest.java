package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.SortParseElement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by liuwuqiang on 2016/11/22.
 */
public class SearchDemoTest {

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
    public void search() {
        SearchResponse response = client.prepareSearch(INDEX, INDEX_2)
                .setTypes(Info.class.getSimpleName(), "type2")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(termQuery("_id", "1001"))                 // Query
//                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();

        SearchHits searchHits = response.getHits();
        for (SearchHit searchHit : searchHits.getHits()) {
            System.out.println(searchHit.getSourceAsString());
        }
    }

    @Test
    public void searchAll() {

        // MatchAll on the whole cluster with all default options
        SearchResponse response = client.prepareSearch().execute().actionGet();

        SearchHits searchHits = response.getHits();
        for (SearchHit searchHit : searchHits.getHits()) {
            System.out.println(searchHit.getSourceAsString());
        }
    }


    // TODO: 2016/11/23
    @Test
    public void searchUseScroll() {
        QueryBuilder qb = termQuery("_id", "1001");

        SearchResponse scrollResp = client.prepareSearch(INDEX)
                .addSort(SortParseElement.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll

        //Scroll until no hits are returned
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                //Handle the hit...
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(60000)).execute().actionGet();

            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }

        }
    }

    @Test
    public void multiSearch() {
        SearchRequestBuilder srb1 = client.prepareSearch()
                .setQuery(QueryBuilders.queryStringQuery("elasticsearch")).setSize(1);
        SearchRequestBuilder srb2 = client.prepareSearch(INDEX)
                .setQuery(QueryBuilders.matchQuery("name", "测试")).setSize(1);

        MultiSearchResponse sr = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .execute().actionGet();

        // You will get all individual responses from MultiSearchResponse#getResponses()
        long nbHits = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            SearchResponse response = item.getResponse();
            nbHits += response.getHits().getTotalHits();
            System.out.println(nbHits);
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsString());
            }
        }
    }

    // TODO: 2016/11/23
    @Test
    public void searchUseAggregation() {
        SearchResponse sr = client.prepareSearch()
                .setQuery(QueryBuilders.matchAllQuery())
                .addAggregation(
                        AggregationBuilders.terms("agg1").field("field")
                )
                .addAggregation(
                        AggregationBuilders.dateHistogram("agg2")
                                .field("birth")
                                .interval(DateHistogramInterval.YEAR)
                )
                .execute().actionGet();

        // Get your facet results
        Terms agg1 = sr.getAggregations().get("agg1");
//        DateHistogram agg2 = sr.getAggregations().get("agg2");
    }

    @Test
    public void searchTerminate() {
        SearchResponse sr = client.prepareSearch(INDEX)
                .setTerminateAfter(1000)
                .get();

        System.out.println(sr.isTerminatedEarly());
        if (sr.isTerminatedEarly()) {
            // We finished early
        }
    }
}
