package com.leederedu.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/2.3/java-docs-bulk-processor.html
 * Created by liuwuqiang on 2016/11/22.
 */
public class BlukDemoTest {

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

    @Test
    public void bulkDemo() throws IOException {
        Info info = new Info().setId(1001).setName("测试").setContext("测试文章内容").setSortNum(2000).setUrl("http://192.168.10.124");

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.add(client.prepareDelete(INDEX, Info.class.getSimpleName(), "1001"));
        bulkRequest.add(client.prepareDelete(INDEX, Info.class.getSimpleName(), "1003"));

        bulkRequest.add(client.prepareIndex(INDEX, Info.class.getSimpleName(), "1004")
                .setSource(objectMapper.writeValueAsBytes(info.setId(1004))));

//        bulkRequest.add(client.prepareUpdate(INDEX, Info.class.getSimpleName(), "1004")
//                .setDoc(jsonBuilder()
//                        .startObject()
//                        .field("name", "测试bulkRequest")
//                        .endObject()));

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            for (BulkItemResponse response : bulkResponse.getItems()) {
                System.out.print("Failed:" + response.isFailed() + " --> " + response.getFailureMessage());
            }
        }
    }

    @Test
    public void bulkProcessor() throws JsonProcessingException, InterruptedException {
        BulkProcessor bulkProcessor = this.getBulkProcessor(client);

        Info info = new Info().setId(1001).setName("测试").setContext("测试文章内容").setSortNum(2000).setUrl("http://192.168.10.124");

        bulkProcessor.add(new IndexRequest(INDEX, Info.class.getSimpleName(), info.getId() + "").source(objectMapper.writeValueAsBytes(info)));
        bulkProcessor.add(new DeleteRequest(INDEX, Info.class.getSimpleName(), "2"));

        //或者 bulkProcessor.close();
        bulkProcessor.awaitClose(10, TimeUnit.MINUTES);

    }


    /**
     * By default, BulkProcessor:
     * sets bulkActions to 1000
     * sets bulkSize to 5mb
     * does not set flushInterval
     * sets concurrentRequests to 1
     * sets backoffPolicy to an exponential backoff with 8 retries and a start delay of 50ms. The total wait time is roughly 5.1 seconds.
     *
     * @param client
     * @return
     */
    private BulkProcessor getBulkProcessor(Client client) {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                        System.out.println("numberOfActions=" + request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        System.out.println(response.hasFailures());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        //This method is called when the bulk failed and raised a Throwable
                        System.err.println(failure);
                    }
                })
                .setBulkActions(10000)                               //We want to execute the bulk every 10 000 requests
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))  //We want to flush the bulk every 1gb
                .setFlushInterval(TimeValue.timeValueSeconds(5))     //We want to flush the bulk every 5 seconds whatever the number of requests
                .setConcurrentRequests(1)                            //Set the number of concurrent requests. A value of 0 means that only a single request will be allowed to be executed. A value of 1 means 1 concurrent request is allowed to be executed while accumulating new bulk requests.
                .setBackoffPolicy(                //Set a custom backoff policy which will initially wait for 100ms, increase exponentially and retries up to three times. A retry is attempted whenever one or more bulk item requests have failed with an EsRejectedExecutionException which indicates that there were too little compute resources available for processing the request. To disable backoff, pass BackoffPolicy.noBackoff()
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
        return bulkProcessor;
    }

}
