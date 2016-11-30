package com.leederedu.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.hasChildQuery;
import static org.elasticsearch.index.query.QueryBuilders.hasParentQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;


/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLJoiningTest extends AbstractTest {

    /**
     * Documents may contains fields of type nested. These fields are used to index arrays of objects,
     * where each object can be queried (with the nested query) as an independent document.
     */
    @Test
    public void nestedQueryTest() {
        QueryBuilder qb = nestedQuery(
                "obj1",
                boolQuery()
                        .must(matchQuery("obj1.name", "blue"))
                        .must(rangeQuery("obj1.count").gt(5))
        )
                .scoreMode("avg");
        executeSearch(qb);
    }

    /**
     * A parent-child relationship can exist between two document types within a single index. The has_child
     * query returns parent documents whose child documents match the specified query, while the has_parent
     * query returns child documents whose parent document matches the specified query.
     */
    @Test
    public void hasChildQueryTest() {
        QueryBuilder qb = hasChildQuery(
                "blog_tag",
                termQuery("tag", "something")
        );
        executeSearch(qb);
    }

    @Test
    public void hasParentQueryTest() {
        QueryBuilder qb = hasParentQuery(
                "blog",
                termQuery("tag", "something")
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
