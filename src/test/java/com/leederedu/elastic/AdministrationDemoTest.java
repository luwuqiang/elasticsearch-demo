package com.leederedu.elastic;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class AdministrationDemoTest {

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
    public void getAdminClient() {
        AdminClient adminClient = client.admin();
    }

    // =================== index admin begin===========================//

    /**
     * 创建索引
     */
    @Test
    public void createIndex() {
        //使用默认配置创建新索引
        client.admin().indices().prepareCreate("twitter").execute().actionGet();

        //使用指定配置创建新索引
        client.admin().indices().prepareCreate("twitter2")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 2)
                )
                .get();
    }

    @Test
    public void putMapping() throws IOException {
        //指定索引的类型映射
        String type = "product";
        XContentBuilder mapping = jsonBuilder()
                .startObject()
                .startObject(type)
                .startObject("properties")
                .startObject("title").field("type", "string").field("store", "yes").endObject()
                .startObject("description").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("price").field("type", "double").endObject()
                .startObject("onSale").field("type", "boolean").endObject()
                .startObject("type").field("type", "integer").endObject()
                .startObject("updateDate").field("type", "date").field("format","yyyy-MM-dd HH:mm:ss").endObject()
                .endObject()
                .endObject()
                .endObject();

        PutMappingRequest mappingRequest = Requests.putMappingRequest(INDEX)
                .type(type).source(mapping);

        client.admin().indices().putMapping(mappingRequest).actionGet();
    }

    /**
     * 创建索引
     */
    @Test
    public void createWithPutMapping() {
        client.admin().indices().prepareCreate("twitter")
                .addMapping("tweet", "{\n" +
                        "    \"tweet\": {\n" +
                        "      \"properties\": {\n" +
                        "        \"message\": {\n" +
                        "          \"type\": \"string\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }")
                .get();

        //The PUT mapping API also allows to add a new type to an existing index:
        client.admin().indices().preparePutMapping("twitter")
                .setType("user")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"name\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}")
                .get();

        // You can also provide the type in the source document
        client.admin().indices().preparePutMapping("twitter")
                .setType("user")
                .setSource("{\n" +
                        "    \"user\":{\n" +
                        "        \"properties\": {\n" +
                        "            \"name\": {\n" +
                        "                \"type\": \"string\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .get();

//        You can use the same API to update an existing mapping:
        client.admin().indices().preparePutMapping("twitter")
                .setType("tweet")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"user_name\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}")
                .get();
    }

    /**
     * 刷新索引
     */
    @Test
    public void refresh() {
        //刷新索引索引
        client.admin().indices().prepareRefresh().get();
        //刷新单个索引
        client.admin().indices()
                .prepareRefresh("twitter")
                .get();
        //刷新多个索引
        client.admin().indices()
                .prepareRefresh("twitter", INDEX)
                .get();
    }

    /**
     * 读取索引的配置
     */
    @Test
    public void getIndexSetting() {
        GetSettingsResponse response = client.admin().indices()
                .prepareGetSettings("twitter", INDEX).get();
        for (ObjectObjectCursor<String, Settings> cursor : response.getIndexToSettings()) {
            Settings settings = cursor.value;
            System.out.println("index=" + cursor.key
                    + ", shards=" + settings.getAsInt("index.number_of_shards", null)
                    + ", replicas=" + settings.getAsInt("index.number_of_replicas", null));
        }
    }

    /**
     * 更新索引配置
     */
    @Test
    public void updateIndexSetting() {
        client.admin().indices().prepareUpdateSettings("twitter")
                .setSettings(Settings.builder()
                        .put("index.number_of_replicas", 0)
                )
                .get();
    }

    // =================== index admin end===========================//

    // =================== cluster admin begin===========================//
    @Test
    public void getAdminCluster() {
        ClusterAdminClient clusterAdminClient = client.admin().cluster();
    }

    @Test
    public void clusterHealth() {
        ClusterHealthResponse healths = client.admin().cluster().prepareHealth().get();
        String clusterName = healths.getClusterName();
        int numberOfDataNodes = healths.getNumberOfDataNodes();
        int numberOfNodes = healths.getNumberOfNodes();

        System.out.println("clusterName=" + clusterName + ", numberOfDataNodes=" + numberOfDataNodes + ", numberOfNodes=" + numberOfNodes);
        Map<String, ClusterIndexHealth> indexMap = healths.getIndices();
        for (Map.Entry<String, ClusterIndexHealth> healthMap : indexMap.entrySet()) {
            ClusterIndexHealth health = healthMap.getValue();
            String index = health.getIndex();
            int numberOfShards = health.getNumberOfShards();
            int numberOfReplicas = health.getNumberOfReplicas();
            ClusterHealthStatus status = health.getStatus();
            System.out.println("index=" + index
                    + ", numberOfShards=" + numberOfShards
                    + ", numberOfReplicas=" + numberOfReplicas
                    + ", ClusterHealthStatus=" + status);
        }
    }

    @Test
    public void waitForStatus() {
//        client.admin().cluster().prepareHealth()
//                .setWaitForGreenStatus()
//                .get();

//        //指定索引，默认超出30秒
//        client.admin().cluster().prepareHealth(INDEX)
//                .setWaitForGreenStatus()
//                .get();
//        System.out.println(System.currentTimeMillis() - time);

        //指定最初等等时间
        ClusterHealthResponse response = client.admin().cluster().prepareHealth(INDEX)
                .setWaitForGreenStatus()
                .setTimeout(TimeValue.timeValueSeconds(2))
                .get();

        ClusterHealthStatus status = response.getIndices().get(INDEX).getStatus();
        if (!status.equals(ClusterHealthStatus.GREEN)) {
            throw new RuntimeException("Index is in " + status + " state");
        }
    }
    // =================== cluster admin end===========================//
}
