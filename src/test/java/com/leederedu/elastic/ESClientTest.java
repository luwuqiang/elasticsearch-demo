package com.leederedu.elastic;

import com.leederedu.elastic.entity.Info;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;

/**
 * 创建Client
 * Created by liuwuqiang on 2016/11/23.
 */
public class ESClientTest {

    public static String INDEX = "leederedu";
    private static String clusterName = "leederedu";

    private String clusterAddresses_Host = "es-node-1:9300,es-node-2:9302,es-node-3:9303,192.168.135.1:9304";

    /**
     * ES下两种客户端对比：
     * 1、TransportClient：轻量级的Client，使用Netty线程池，Socket连接到ES集群。本身不加入到集群，只作为请求的处理。
     * 2、Node Client：客户端节点本身也是ES节点，加入到集群，和其他ElasticSearch节点一样。频繁的开启和关闭这类Node Clients会在集群中产生“噪音”。
     */
    public void createClient() {
        //TransportClient客户端
        Client client = ESClient.getClient();

        //Node Client客户端
    }

    @Test
    public void startNode() {
//        1、创建节点并加入集群
        //启动节点
//        Settings.Builder builder = Settings.settingsBuilder();
//        builder.put("path.home", "D:/lucene/data/index");
//        Node node = nodeBuilder().settings(builder)
//                .clusterName(clusterName)
//                .client(true)//把node.data设置成false或 node.client设置成true时，该节点不保存数据，仅作为客户端
//                .node();
//        Client client = node.client();


        Settings settings = Settings.settingsBuilder()
//                .put("client.transport.ping_timeout", 1000)
                .put("path.home", "D:\\pro_ks\\main\\nodeclient\\elasticsearch-2.3.5-node-4")
//                .put("discovery.zen.ping.multicast.enabled", "false").put("timeout", 1)
//                .putArray("discovery.zen.ping.unicast.hosts", clusterAddresses_Host.split(","))
                .build();
        Node node = NodeBuilder.nodeBuilder().clusterName(clusterName).client(true).settings(settings).node();
        Client client = node.client();

        GetResponse response = client
                .prepareGet(INDEX, Info.class.getSimpleName(), "1002")
                .setOperationThreaded(false)
                .get();

        if (response.getSourceAsBytes() != null) {
//            Info info = objectMapper.readValue(response.getSourceAsBytes(), Info.class);
//            System.out.println(info.toString());
            System.out.println(response.getSourceAsString());
        }
        //关闭节点
        node.close();

//        2、创建本地节点，但不加入集群，一般用于本地测试
//        Node node = nodeBuilder().local(true).node();

    }
}
