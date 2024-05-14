package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Route;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface RouteRepository extends Neo4jRepository<Route,Long>,Importable {
    @Query("// add the routes\n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/routes.txt' AS csv\n" +
            "MATCH (a:Agency {id: csv.agency_id})\n" +
            "CREATE (a)-[:OPERATES]->(r:Route {id: csv.route_id, short_name: csv.route_short_name, long_name: csv.route_long_name, type: toInt(csv.route_type)});\n")
    void loadNodes ();

    //Route findByRouteId(@Param("routeId") String routeId, @Depth @Param("depth") int depth);

    @Query("MATCH (r:Route {id: $routeId})-[*1..$depth]-(related) RETURN r, collect(related)")
    Route findByRouteIdWithDepth(@Param("routeId") String routeId, @Param("depth") int depth);
}
