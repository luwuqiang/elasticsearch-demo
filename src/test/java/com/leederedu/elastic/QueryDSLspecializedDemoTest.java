package com.leederedu.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.moreLikeThisQuery;
import static org.elasticsearch.index.query.QueryBuilders.scriptQuery;
import static org.elasticsearch.index.query.QueryBuilders.templateQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLspecializedDemoTest extends AbstractTest {
    /**
     * This query finds documents which are similar to the specified text, document, or collection of documents.
     */
    @Test
    public void moreLikeThisQueryTest() {
        QueryBuilder qb = moreLikeThisQuery("name.first", "name.last")
                .like("text like this one")
                .minTermFreq(1)
                .maxQueryTerms(12);
        executeSearch(qb);
    }

    /**
     * The template query accepts a Mustache template (either inline, indexed, or from a file), and a map of parameters, and combines the two to generate the final query to execute.
     */
    @Test
    public void templateQueryTest() {
        Map<String, Object> template_params = new HashMap<>();
        template_params.put("param_gender", "male");
        // TODO: 2016/11/23
//        You can use your stored search templates in config/scripts. For example, if you have a file named config/scripts/template_gender.mustache containing:
//        {"template" : {"query" : {"match" : {"gender" : "{{param_gender}}"}}}}


        QueryBuilder qb = templateQuery(
                "gender_template",
                ScriptService.ScriptType.FILE,
                template_params);

        /**
         // 或者
         // You can also store your template in a special index named .scripts:
         client.preparePutIndexedScript("mustache", "template_gender",
         "{\n" +
         "    \"template\" : {\n" +
         "        \"query\" : {\n" +
         "            \"match\" : {\n" +
         "                \"gender\" : \"{{param_gender}}\"\n" +
         "            }\n" +
         "        }\n" +
         "    }\n" +
         "}").get();

         QueryBuilder qb = templateQuery(
         "gender_template",
         ScriptService.ScriptType.INDEXED,
         template_params);
         */

        executeSearch(qb);
    }

    /**
     * This query allows a script to act as a filter. Also see the function_score query.
     */
    @Test
    public void scriptQueryTest() {
        QueryBuilder qb = scriptQuery(
                new Script("doc['num1'].value > 1")
        );

        /**
         //或者
         //If you have stored on each data node a script named mygroovyscript.groovy with:
         //doc['num1'].value > param1
         QueryBuilder qb = scriptQuery(
         new Script(
         "mygroovyscript",
         ScriptService.ScriptType.FILE,
         "groovy",
         ImmutableMap.of("param1", 5))
         );
         */
        executeSearch(qb);
    }

    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
