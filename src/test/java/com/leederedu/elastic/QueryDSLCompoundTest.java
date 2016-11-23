package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.elasticsearch.index.query.QueryBuilders.andQuery;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.disMaxQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.functionScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.indicesQuery;
import static org.elasticsearch.index.query.QueryBuilders.limitQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.notQuery;
import static org.elasticsearch.index.query.QueryBuilders.orQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.exponentialDecayFunction;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.randomFunction;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLCompoundTest {

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

    /**
     * A query which wraps another query, but executes it in filter context. All matching documents are given the same “constant” _score.
     */
    public void constantScoreQueryTest() {
        QueryBuilder qb = constantScoreQuery(
                termQuery("name", "kimchy")).boost(2.0f);
        executeSearch(qb);
    }


    /**
     * The default query for combining multiple leaf or compound query clauses, as must, should, must_not, or
     * filter clauses. The must and should clauses have their scores combined — the more matching clauses,
     * the better — while the must_not and filter clauses are executed in filter context.
     */
    @Test
    public void boolQueryTest() {
        QueryBuilder qb = boolQuery()
                .must(termQuery("content", "test1"))
                .must(termQuery("content", "test4"))
                .mustNot(termQuery("content", "test2"))
                .should(termQuery("content", "test3"));
        executeSearch(qb);
    }

    /**
     * A query which accepts multiple queries, and returns any documents which match any of the query clauses.
     * While the bool query combines the scores from all matching queries, the dis_max query uses the score of
     * the single best- matching query clause.
     */
    @Test
    public void disMaxQueryTest() {
        QueryBuilder qb = disMaxQuery()
                .add(termQuery("name", "kimchy"))
                .add(termQuery("name", "elasticsearch"))
                .boost(1.2f)
                .tieBreaker(0.7f);
        executeSearch(qb);
    }

    /**
     * Modify the scores returned by the main query with functions to take into account factors like popularity,
     * recency, distance, or custom algorithms implemented with scripting.
     */
    @Test
    public void functionScoreQueryTest() {
        QueryBuilder qb = functionScoreQuery()
                .add(
                        matchQuery("name", "kimchy"),
                        randomFunction("ABCDEF")
                )
                .add(
                        exponentialDecayFunction("age", 0L, 1L)
                );
        executeSearch(qb);
    }

    /**
     * Return documents which match a positive query, but reduce the score of documents which also match a negative query.
     */
    @Test
    public void boostingQueryTest() {
        QueryBuilder qb = boostingQuery()
                .positive(termQuery("name", "kimchy"))
                .negative(termQuery("name", "dadoonet"))
                .negativeBoost(0.2f);
        executeSearch(qb);
    }

    /**
     * Execute one query for the specified indices, and another for other indices.
     */
    @Test
    public void indicesQueryTest() {
        // Using another query when no match for the main one
        QueryBuilder qb = indicesQuery(
                termQuery("tag", "wow"),
                "index1", "index2"
        ).noMatchQuery(termQuery("tag", "kow"));

//        // Using all (match all) or none (match no documents)
//        QueryBuilder qb = indicesQuery(
//                termQuery("tag", "wow"),
//                "index1", "index2"
//        ).noMatchQuery("all");

        executeSearch(qb);
    }

    /**
     * Synonyms for the bool query.
     */
    @Test
    public void andQueryTest() {
        QueryBuilder qb = andQuery(
                rangeQuery("postDate").from("2010-03-01").to("2010-04-01"),
                prefixQuery("name.second", "ba"));
        executeSearch(qb);
    }

    /**
     * Synonyms for the bool query.
     */
    @Test
    public void notQueryTest() {
        QueryBuilder qb = notQuery(
                rangeQuery("price").from("1").to("2")
        );
        executeSearch(qb);
    }

    /**
     * Synonyms for the bool query.
     */
    @Test
    public void orQueryTest() {
        QueryBuilder qb = orQuery(
                rangeQuery("price").from(1).to(2),
                matchQuery("name", "joe")
        );
        executeSearch(qb);
    }

    /**
     * Combine a query clause in query context with another in filter context. [2.0.0]
     */
    @Test
    public void filteredQueryTest() {
        QueryBuilder qb = filteredQuery(
                matchQuery("name", "kimchy"),
                rangeQuery("dateOfBirth").from("1900").to("2100")
        );
        executeSearch(qb);
    }

    /**
     * Limits the number of documents examined per shard
     */
    @Test
    public void limitQueryTest() {
        QueryBuilder qb = limitQuery(100);
        executeSearch(qb);
    }

    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
