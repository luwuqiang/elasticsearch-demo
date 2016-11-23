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

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.missingQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.typeQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLTermLevelTest {

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
     * Find documents which contain the exact term specified in the field specified.
     */
    @Test
    public void termQueryTest() {
        QueryBuilder qb = termQuery(
                "_id",
                "1001"
        );
        executeSearch(qb);
    }

    /**
     * Find documents which contain any of the exact terms specified in the field specified.
     */
    @Test
    public void termsQueryTest() {
        QueryBuilder qb = termsQuery("_id",
                "1001", "1102");
        executeSearch(qb);
    }

    /**
     * Find documents where the field specified contains values (dates, numbers, or strings) in the range specified.
     */
    @Test
    public void rangeQueryTest() {
        QueryBuilder qb = rangeQuery("_id")
                .from(1002)
                .to(2001)
                .includeLower(true)
                .includeUpper(false);
        executeSearch(qb);

        // A simplified form using gte, gt, lt or lte
//        QueryBuilder qb = rangeQuery("age")
//                .gte("10")
//                .lt("20");
    }

    /**
     * Find documents where the field specified contains any non-null value.
     */
    @Test
    public void existsQueryTest() {
        QueryBuilder qb = existsQuery("name");
        executeSearch(qb);
    }

    /**
     * Find documents where the field specified does is missing or contains only null values.
     */
    @Test
    public void missingQueryTest() {
        QueryBuilder qb = missingQuery("user")
                .existence(true)
                .nullValue(true);
        executeSearch(qb);
    }

    /**
     * Find documents where the field specified contains terms which being with the exact prefix specified.
     */
    @Test
    public void prefixQueryTest() {
        QueryBuilder qb = prefixQuery(
                "name", //field
                "你" //prefix
        );
        executeSearch(qb);
    }

    /**
     * Find documents where the field specified contains terms which match the pattern specified, where the pattern supports single character wildcards (?) and multi-character wildcards (*)
     */
    @Test
    public void wildcardQueryTest() {
        QueryBuilder qb = wildcardQuery("user", "k?mc*");
        executeSearch(qb);

    }

    /**
     * Find documents where the field specified contains terms which match the regular expression specified.
     */
    @Test
    public void regexpQueryTest() {
        QueryBuilder qb = regexpQuery(
                "name.first",
                "s.*y");
        executeSearch(qb);
    }

    /**
     * Find documents where the field specified contains terms which are fuzzily similar to the specified term. Fuzziness is measured as a Levenshtein edit distance of 1 or 2.
     */
    @Test
    public void fuzzyQueryTest() {
        QueryBuilder qb = fuzzyQuery(
                "name",
                "kimzhy"
        );
        executeSearch(qb);
    }

    /**
     * Find documents of the specified type.
     */
    @Test
    public void typeQueryTest() {
        QueryBuilder qb = typeQuery("my_type");
        executeSearch(qb);
    }

    /**
     * Find documents with the specified type and IDs.
     */
    @Test
    public void idsQueryTest() {
        QueryBuilder qb = idsQuery("Info", "type2")
                .addIds("1", "4", "100");

//        QueryBuilder qb = idsQuery()
//                .addIds("1", "4", "100");
        executeSearch(qb);
    }

    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }


}
