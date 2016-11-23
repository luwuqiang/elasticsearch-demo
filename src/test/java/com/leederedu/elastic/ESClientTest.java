package com.leederedu.elastic;

import org.elasticsearch.client.Client;

/**
 * 创建Client
 * Created by liuwuqiang on 2016/11/23.
 */
public class ESClientTest {

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
}
