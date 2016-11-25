package com.leederedu.elastic;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.junit.Test;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * 创建Client
 * Created by liuwuqiang on 2016/11/23.
 */
public class ESClientTest {

    private static String clusterName = "elasticsearch";
    public static String clusterAddresses = "192.168.135.134:9300,192.168.135.134:9302,192.168.135.134:9303";
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
        Node node = nodeBuilder()
                .clusterName(clusterName)
                .client(true)//把node.data设置成false或 node.client设置成true时，该节点不保存数据，仅作为客户端
//                .settings()
                .node();
        Client client = node.client();
        //关闭节点
        node.close();

//        2、创建本地节点，但不加入集群，一般用于本地测试
//        Node node = nodeBuilder().local(true).node();

    }
}
