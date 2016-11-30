package com.leederedu.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.junit.Test;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class AggregationDemoTest  extends AbstractTest{

    public void aggregation() {
        // TODO: 2016/11/23 需要更换成node client
//        SearchResponse sr = client.prepareSearch()
//                .setQuery( /* your query */ )
//                .addAggregation( /* add an aggregation */ )
//                .execute().actionGet();

    }

    @Test
    public void aggregationTest() {
        SearchResponse sr = client.prepareSearch()
                .addAggregation(
                        AggregationBuilders.terms("by_country").field("country")
                                .subAggregation(AggregationBuilders.dateHistogram("by_year")
                                        .field("dateOfBirth")
                                        .interval(DateHistogramInterval.YEAR)
                                        .subAggregation(AggregationBuilders.avg("avg_children").field("children"))
                                )
                )
                .execute().actionGet();

    }
}
