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

import static org.elasticsearch.index.query.QueryBuilders.commonTermsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLFullTextDemoTest {

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
     * 查找所有数据
     */
    @Test
    public void matchAllQueryTest() {
        QueryBuilder qb = matchAllQuery();
        executeSearch(qb);
    }

    // ================================= Full Text Queries begin ========================
    /**
     * 单字段匹配查找
     */
    @Test
    public void matchQueryTest() {
        QueryBuilder qb = matchQuery(
                "id",  //field
                "1004" //查询关键字内容
        );
        executeSearch(qb);
    }

    /**
     * 多字段匹配查找
     */
    @Test
    public void multiMatchQueryTest() {
        QueryBuilder qb = multiMatchQuery(
                "测试",  //查询关键字内容
                "name", "context" //多个field
        );
        executeSearch(qb);
    }

    /**
     *
     */
    @Test
    public void commonTermsQueryTest() {
        QueryBuilder qb = commonTermsQuery("context",
                "测");

        executeSearch(qb);
    }

    @Test
    public void queryStringQueryTest() {
        QueryBuilder qb = matchQuery(
                "name",
                "试");
        executeSearch(qb);
    }

    /**
     * The simple_query_string supports the following special characters:
     * <p>
     * + signifies AND operation
     * | signifies OR operation
     * - negates a single token
     * " wraps a number of tokens to signify a phrase for searching
     * * at the end of a term signifies a prefix query
     * ( and ) signify precedence
     * ~N after a word signifies edit distance (fuzziness)
     * ~N after a phrase signifies slop amount
     * In order to search for any of these special characters, they will need to be escaped with \.
     */
    @Test
    public void simpleQueryStringQueryTest() {
        QueryBuilder qb = simpleQueryStringQuery("+测试 -elasticsearch");
        executeSearch(qb);
    }

    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }

    // ================================= Full Text Queries end ========================




}
