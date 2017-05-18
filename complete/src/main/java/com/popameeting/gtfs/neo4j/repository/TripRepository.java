package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Trip;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface TripRepository extends GraphRepository<Trip>,Importable {
    @Query("// add the trips\n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/trips.txt' AS csv\n" +
            "MATCH (r:Route {id: csv.route_id})\n" +
            "MERGE (r)<-[:USES]-(t:Trip {id: csv.trip_id, service_id: csv.service_id});")
    void loadNodes ();
}
