package com.leederedu.elastic;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created by liuwuqiang on 2016/11/28.
 */
public class AnalyzeDemoTest extends AbstractTest{


    @Test
    public void test() {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        AnalyzeRequestBuilder request = new AnalyzeRequestBuilder(indicesAdminClient, AnalyzeAction.INSTANCE, INDEX, "中华人民共和国国歌");
         request.setAnalyzer("ik_pinyin");
//        request.setTokenizer("ik");
//        request.setTokenizer("ik_smart");
        // Analyzer（分析器）、Tokenizer（分词器）
        List<AnalyzeResponse.AnalyzeToken> listAnalysis = request.execute().actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken analyzeToken : listAnalysis) {
            System.out.println(analyzeToken.getTerm());
        }

//        ik分词结果：中华人民共和国、中华人民、中华、华人、人民共和国、人民、共和国、共和、国、国歌
//        ik只能分词：中华人民共和国、国歌
    }

    /**
     * 自定义分词器,建议将自定义分词配置在elasticsearch.yml配置中
     *
     * @throws IOException
     */
    @Test
    public void createCustomAnlyzer() throws IOException, ExecutionException, InterruptedException {
        //分词器名称：ik_pinyin_analyzer
        String customAnalyer = "{"
                + "	\"index\" : {"
                + "		\"analysis\" : {"
                + "			\"analyzer\" : {"
                + "				\"ik_pinyin_analyzer\" : {"
                + "					\"type\":\"custom\","
                + "					\"tokenizer\" : \"ik_smart\","
                + "					\"filter\" : [\"my_pinyin\",\"word_delimiter\"]"
                + "				}"
                + "			},"
                + "			\"filter\" : {"
                + "				\"my_pinyin\" : {"
                + "					\"type\" : \"pinyin\","
                + "					\"first_letter\" : \"none\","
                + "					\"padding_char\" : \" \""
                + "				}"
                + "			}"
                + "		}"
                + "	}"
                + "}";

        client.admin().indices().prepareCreate(INDEX)
//                .setSettings(customAnalyer)
                .execute().actionGet();
//        client.admin().indices().prepareUpdateSettings(INDEX).setSettings(Settings.builder()
//                .put("index.number_of_replicas", "3")).execute().actionGet();

    }

    @Test
    public void createIKMapping() throws IOException, ExecutionException, InterruptedException {
        String type = "Info2";
        XContentBuilder mapping = jsonBuilder().startObject()
                // 索引库名（类似数据库中的表）
                .startObject(type).startObject("properties")
                //不指定检索分词器[search_analyzer]时，默认使用索引的分词器
                //如：name2、name3的检索分词器将默认分别为ik、ik_smart
                .startObject("name").field("type", "string").field("analyzer", "ik").field("search_analyzer", "ik_smart").endObject()
                .startObject("name2").field("type", "string").field("analyzer", "ik").endObject()
                .startObject("name3").field("type", "string").field("analyzer", "ik_smart").endObject()
                //不指定分词器，将使用lucene自带分词器（中文分词准确率很低）
                .startObject("name4").field("type", "string").endObject()
                //该字段不分词
                .startObject("name5").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("name6").field("type", "string").field("analyzer", "ik_pinyin").field("search_analyzer", "ik_smart").endObject()
                .endObject().endObject().endObject();

        //即对几个字段做索引时使用ik分词器即ik_max_word，在搜索时使用ik_smart
        client.admin().indices().preparePutMapping(INDEX).setType(type).setSource(mapping).execute().get();
    }

    @Test
    public void testIK() throws IOException {
        String type = "Info2";

        XContentBuilder doc = jsonBuilder().startObject().field("name", "中华人民共和国国歌")
                .field("name2", "中华人民共和国国歌").field("name3", "中华人民共和国国歌")
                .field("name4", "中华人民共和国国歌").field("name5", "中华人民共和国国歌")
                .field("name6", "中华人民共和国国歌").endObject();
        client.prepareIndex(INDEX, type).setId("1").setSource(doc).execute().actionGet();

        doc = jsonBuilder().startObject().field("name", "中华人民")
                .field("name2", "人民").field("name3", "中华人民")
                .field("name4", "中华人民").field("name5", "中华人民").field("name6", "中华人民").endObject();
        client.prepareIndex(INDEX, type).setId("2").setSource(doc).execute().actionGet();

        doc = jsonBuilder().startObject()
                .field("name2", "人").field("name3", "人")
                .field("name4", "人").field("name5", "人").field("name6", "人").endObject();
        client.prepareIndex(INDEX, type).setId("3").setSource(doc).execute().actionGet();

    }

    @Test
    public void testQueryIK() {
        QueryBuilder qb = multiMatchQuery("中华人民共和国", "name");
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(1, response.getHits().totalHits());
        Assert.assertEquals("1", response.getHits().hits()[0].getId());

        qb = multiMatchQuery("中华人民共和国", "name2");
        response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(2, response.getHits().totalHits());
        Assert.assertEquals("1", response.getHits().hits()[0].getId());
        Assert.assertEquals("2", response.getHits().hits()[1].getId());

        qb = multiMatchQuery("中华人民", "name3");
        response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(1, response.getHits().totalHits());
        Assert.assertEquals("2", response.getHits().hits()[0].getId());

        qb = multiMatchQuery("中华人民", "name4");
        response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(3, response.getHits().totalHits());

        qb = multiMatchQuery("人", "name5");
        response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(1, response.getHits().totalHits());
        Assert.assertEquals("3", response.getHits().hits()[0].getId());

        qb = multiMatchQuery("zhrmghg", "name6");
        response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        Assert.assertEquals(1, response.getHits().totalHits());
        Assert.assertEquals("1", response.getHits().hits()[0].getId());
    }

    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
