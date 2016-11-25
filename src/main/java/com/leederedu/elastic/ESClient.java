/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.leederedu.elastic;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class ESClient extends LuceneTestCase {

    /**
     * Defaults to localhost:9300
     */
    public static final String TESTS_CLUSTER_DEFAULT = "localhost:9300";

    protected static final ESLogger logger = ESLoggerFactory.getLogger(ESClient.class.getName());

    private static final AtomicInteger counter = new AtomicInteger();
    private static Client client;
    private static String clusterAddresses;
//    protected String index;

    private static Client startClient(TransportAddress... transportAddresses) {
        Settings clientSettings = Settings.settingsBuilder()
                .put("name", "qa_smoke_client_" + counter.getAndIncrement())
                .put(InternalSettingsPreparer.IGNORE_SYSTEM_PROPERTIES_SETTING, true) // prevents any settings to be replaced by system properties.
                .put("client.transport.ignore_cluster_name", true)
                .put("node.mode", "network").build(); // we require network here!

        TransportClient.Builder transportClientBuilder = TransportClient.builder().settings(clientSettings);
        TransportClient client = transportClientBuilder.build().addTransportAddresses(transportAddresses);

        logger.info("--> Elasticsearch Java ElasticClient started");

        try {
            ClusterHealthResponse health = client.admin().cluster().prepareHealth().get();
            logger.info("--> connected to [{}] cluster which is running [{}] node(s).",
                    health.getClusterName(), health.getNumberOfNodes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return client;
    }

    private static Client startClient() throws UnknownHostException {
        String[] stringAddresses = clusterAddresses.split(",");
        TransportAddress[] transportAddresses = new TransportAddress[stringAddresses.length];
        int i = 0;
        for (String stringAddress : stringAddresses) {
            String[] split = stringAddress.split(":");
            if (split.length < 2) {
                throw new IllegalArgumentException("address [" + clusterAddresses + "] not valid");
            }
            try {
                transportAddresses[i++] = new InetSocketTransportAddress(InetAddress.getByName(split[0]), Integer.valueOf(split[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("port is not valid, expected number but was [" + split[1] + "]");
            }
        }
        return startClient(transportAddresses);
    }

    public static Client getClient() {
        if (client == null) {
            try {
                client = startClient();
            } catch (UnknownHostException e) {
                logger.error("can not start the client", e);
            }
        }
        return client;
    }

    public static void initializeSettings() throws UnknownHostException {
        if (clusterAddresses == null || clusterAddresses.isEmpty()) {
            clusterAddresses = TESTS_CLUSTER_DEFAULT;
            logger.info("Falling back to [{}]", TESTS_CLUSTER_DEFAULT);
        }
    }

    public static void closeTransportClient() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    public static void setClusterAddresses(String clusterAddresses) {
        ESClient.clusterAddresses = clusterAddresses;
    }
}
