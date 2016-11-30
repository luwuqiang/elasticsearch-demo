package com.leederedu.elastic;

import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class PercolateDemoTest extends AbstractTest {

    @Test
    public void percolate() throws IOException {
        //This is the query we're registering in the percolator
        QueryBuilder qb = termQuery("content", "amazing");

        //Index the query = register it in the percolator
        client.prepareIndex("myIndexName", ".percolator", "myDesignatedQueryName")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("query", qb) // Register the query
                        .endObject())
                .setRefresh(true) // Needed when the query shall be available immediately
                .execute().actionGet();

//        ------------------------------------------------------------------
//        This indexes the above term query under the name myDesignatedQueryName.
//        In order to check a document against the registered queries, use this code:

        //Build a document to check against the percolator
        XContentBuilder docBuilder = XContentFactory.jsonBuilder().startObject();
        docBuilder.field("doc").startObject(); //This is needed to designate the document
        docBuilder.field("content", "This is amazing!");
        docBuilder.endObject(); //End of the doc field
        docBuilder.endObject(); //End of the JSON root object
        //Percolate
        PercolateResponse response = client.preparePercolate()
                .setIndices("myIndexName")
                .setDocumentType("myDocumentType")
                .setSource(docBuilder).execute().actionGet();
        //Iterate over the results
        for (PercolateResponse.Match match : response) {
            //Handle the result which is the name of
            //the query in the percolator
        }
    }
}
