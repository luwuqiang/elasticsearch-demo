package com.leederedu.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.elasticsearch.index.query.QueryBuilders.geoDistanceQuery;
import static org.elasticsearch.index.query.QueryBuilders.geoDistanceRangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.geoHashCellQuery;
import static org.elasticsearch.index.query.QueryBuilders.geoPolygonQuery;
import static org.elasticsearch.index.query.QueryBuilders.geoShapeQuery;

/**
 * Created by liuwuqiang on 2016/11/23.
 */
public class QueryDSLGeoTest {

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

    /**
     * Find document with geo-shapes which either intersect, are contained by, or do not intersect with the specified geo-shape.
     */
    @Test
    public void geo_shapeQueryTest() {
        QueryBuilder qb = geoShapeQuery(
                "pin.location",
                ShapeBuilder.newMultiPoint()
                        .point(0, 0)
                        .point(0, 10)
                        .point(10, 10)
                        .point(10, 0)
                        .point(0, 0),
                ShapeRelation.WITHIN);

        // Using pre-indexed shapes
//        QueryBuilder qb = geoShapeQuery(
//                "pin.location",
//                "DEU",
//                "countries",
//                ShapeRelation.WITHIN)
//                .indexedShapeIndex("shapes")
//                .indexedShapePath("location");

        executeSearch(qb);
    }

    /**
     * Finds documents with geo-points that fall into the specified rectangle.
     */
    @Test
    public void geo_bounding_boxQueryTest() {
        // Using pre-indexed shapes
        QueryBuilder qb = geoShapeQuery(
                "pin.location",
                "DEU",
                "countries",
                ShapeRelation.WITHIN)
                .indexedShapeIndex("shapes")
                .indexedShapePath("location");
        executeSearch(qb);
    }

    /**
     * Finds document with geo-points within the specified distance of a central point.
     */
    @Test
    public void geoDistanceQueryTest() {
        QueryBuilder qb = geoDistanceQuery("pin.location")
                .point(40, -70)
                .distance(200, DistanceUnit.KILOMETERS)
                .optimizeBbox("memory")
                .geoDistance(GeoDistance.ARC);
        executeSearch(qb);
    }

    /**
     * Like the geo_point query, but the range starts at a specified distance from the central point.
     */
    @Test
    public void geoDistanceRangeQueryTest() {
        QueryBuilder qb = geoDistanceRangeQuery("pin.location")
                .point(40, -70)
                .from("200km")
                .to("400km")
                .includeLower(true)
                .includeUpper(false)
                .optimizeBbox("memory")
                .geoDistance(GeoDistance.ARC);
        executeSearch(qb);
    }

    /**
     * Find documents with geo-points within the specified polygon.
     */
    @Test
    public void geoPolygonQueryTest() {
        QueryBuilder qb = geoPolygonQuery("pin.location")
                .addPoint(40, -70)
                .addPoint(30, -80)
                .addPoint(20, -90);
        executeSearch(qb);
    }

    /**
     * Find geo-points whose geohash intersects with the geohash of the specified point.
     */
    @Test
    public void geoHashCellQueryTest() {
        QueryBuilder qb = geoHashCellQuery("pin.location",
                new GeoPoint(13.4080, 52.5186))
                .neighbors(true)
                .precision(3);
        executeSearch(qb);
    }


    private void executeSearch(QueryBuilder qb) {
        SearchResponse response = client.prepareSearch(INDEX).setQuery(qb).execute().actionGet();
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
