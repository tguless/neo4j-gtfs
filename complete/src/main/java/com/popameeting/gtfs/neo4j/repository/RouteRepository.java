package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Route;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface RouteRepository extends GraphRepository<Route>,Importable {
    @Query("// add the routes\n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/routes.txt' AS csv\n" +
            "MATCH (a:Agency {id: csv.agency_id})\n" +
            "CREATE (a)-[:OPERATES]->(r:Route {id: csv.route_id, short_name: csv.route_short_name, long_name: csv.route_long_name, type: toInt(csv.route_type)});\n")
    void loadNodes ();
}
