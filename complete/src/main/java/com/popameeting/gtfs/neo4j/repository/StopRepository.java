package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Stop;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface StopRepository extends Neo4jRepository<Stop, Long> {
    @Query(
            "//connect parent/child relationships to stops\n" +
            "load csv with headers from\n" +
            "'file:///nmbs/stops.txt' as csv\n" +
            "with csv\n" +
            "  where not (csv.parent_station is null)\n" +
            "match (ps:Stop {id: csv.parent_station}), (s:Stop {id: csv.stop_id})\n" +
            "create (ps)<-[:PART_OF]-(s);")
    void connectParentChild ();

    @Query( "//add the stops\n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/stops.txt' AS csv\n" +
            "CREATE (s:Stop {id: csv.stop_id, name: csv.stop_name, lat: toFloat(csv.stop_lat), lon: toFloat(csv.stop_lon), platform_code: csv.platform_code, parent_station: csv.parent_station, location_type: csv.location_type});\n")
    void addStops();

    Stop findByName(@Param("stopName") String stopName,@Depth @Param("depth") int depth);

}
