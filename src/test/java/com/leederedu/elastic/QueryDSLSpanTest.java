package com.leederedu.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanContainingQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanFirstQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanMultiTermQueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.spanNearQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanNotQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanOrQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanTermQuery;
import static org.elasticsearch.index.query.QueryBuilders.spanWithinQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLSpanTest extends AbstractTest {

    /**
     * The equivalent of the term query but for use with other span queries.
     */
    @Test
    public void spanTermQueryTest() {
        QueryBuilder qb = spanTermQuery(
                "user",
                "kimchy");
        executeSearch(qb);
    }

    /**
     * Wraps a term, range, prefix, wildcard, regexp, or fuzzy query.
     */
    @Test
    public void spanMultiTermQueryBuilderqueryTest() {
        QueryBuilder qb = spanMultiTermQueryBuilder(
                prefixQuery("user", "ki"));
        executeSearch(qb);
    }

    /**
     * Accepts another span query whose matches must appear within the first N positions of the field.
     */
    @Test
    public void spanFirstQueryTest() {
        QueryBuilder qb = spanFirstQuery(
                spanTermQuery("user", "kimchy"),
                3);
        executeSearch(qb);
    }

    /**
     * Accepts multiple span queries whose matches must be within the specified distance of each other, and possibly in the same order.
     */
    @Test
    public void spanNearQueryTest() {
        QueryBuilder qb = spanNearQuery()
                .clause(spanTermQuery("field", "value1"))
                .clause(spanTermQuery("field", "value2"))
                .clause(spanTermQuery("field", "value3"))
                .slop(12)
                .inOrder(false)
                .collectPayloads(false);
        executeSearch(qb);
    }

    /**
     * Combines multiple span queries — returns documents which match any of the specified queries.
     */
    @Test
    public void spanOrQueryTest() {
        QueryBuilder qb = spanOrQuery()
                .clause(spanTermQuery("field", "value1"))
                .clause(spanTermQuery("field", "value2"))
                .clause(spanTermQuery("field", "value3"));
        executeSearch(qb);
    }

    /**
     * Wraps another span query, and excludes any documents which match that query.
     */
    @Test
    public void spanNotQueryTest() {
        QueryBuilder qb = spanNotQuery()
                .include(spanTermQuery("field", "value1"))
                .exclude(spanTermQuery("field", "value2"));
        executeSearch(qb);
    }

    /**
     * Accepts a list of span queries, but only returns those spans which also match a second span query.
     */
    @Test
    public void spanContainingQueryTest() {
        QueryBuilder qb = spanContainingQuery()
                .little(spanTermQuery("field1", "foo"))
                .big(spanNearQuery()
                        .clause(spanTermQuery("field1", "bar"))
                        .clause(spanTermQuery("field1", "baz"))
                        .slop(5)
                        .inOrder(true)
                );
        executeSearch(qb);
    }

    /**
     * The result from a single span query is returned as long is its span falls within the spans returned by a list of other span queries.
     */
    @Test
    public void spanWithinQueryTest() {
        QueryBuilder qb = spanWithinQuery()
                .little(spanTermQuery("field1", "foo"))
                .big(spanNearQuery()
                        .clause(spanTermQuery("field1", "bar"))
                        .clause(spanTermQuery("field1", "baz"))
                        .slop(5)
                        .inOrder(true)
                );
        executeSearch(qb);
    }


    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
