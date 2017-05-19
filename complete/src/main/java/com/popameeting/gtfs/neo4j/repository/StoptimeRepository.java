package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Stop;
import com.popameeting.gtfs.neo4j.entity.Stoptime;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface StoptimeRepository extends Neo4jRepository<Stoptime, Long> {


    @Query("//add the stoptimes\n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/stop_times.txt' AS csv\n" +
            "MATCH (t:Trip {id: csv.trip_id}), (s:Stop {id: csv.stop_id})\n" +
            "CREATE (t)<-[:PART_OF_TRIP]-(st:Stoptime {arrival_time: csv.arrival_time, departure_time: csv.departure_time, stop_sequence: toInt(csv.stop_sequence)})-[:LOCATED_AT]->(s);\n")
    void addStopTimes();

    @Query( "//create integers out of the stoptimes (to allow for calculations/ordering)\n" +
            "MATCH (s:Stoptime)\n" +
            "SET s.arrival_time_int=toInt(replace(s.arrival_time,':',''))/100\n" +
            "SET s.departure_time_int=toInt(replace(s.departure_time,':',''))/100\n" +
            "; ")
    void stopTimeToInt();

    @Query(
            "//connect the stoptime sequences\n" +
            "MATCH (s1:Stoptime)-[:PART_OF_TRIP]->(t:Trip),\n" +
            "      (s2:Stoptime)-[:PART_OF_TRIP]->(t)\n" +
            "WHERE s2.stop_sequence=s1.stop_sequence + 1 \n" +
            "CREATE (s1)-[:PRECEDES]->(s2);")
    void connectSequences();

    /*
        I got the sort parameter to pass in using spring data rest, but there seems to be a bug because the order is not
        coming back correct.
        http://localhost:8080/stoptimes/search/getMyTrips?sort=stopSequence,asc
        http://localhost:8080/stoptimes/search/getMyTrips?sort=departureTimeInt,asc
    */
    @Query("//find a DIRECT route with range conditions\n" +
            "MATCH\n" +
            "  (orig:Stop {name: 'WESTWOOD'})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)\n" +
            "WHERE\n"+
            "  orig_st.departure_time > '06:30:00'\n" +
            "  AND orig_st.departure_time < '07:10:00'\n" +
            "  AND trp.service_id='4'\n" +
            "WITH\n"+
            "  orig, orig_st\n" +
            "MATCH\n" +
            "    (dest:Stop {name:'HOBOKEN'})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)\n" +
            "WHERE\n"+
            "    dest_st.arrival_time < '08:00:00'\n" +
            "    AND dest_st.arrival_time > '07:00:00'\n" +
            "    AND dest_st.arrival_time > orig_st.departure_time\n"+
            "    AND trp2.service_id='4'\n" +
            "WITH\n"+
            "    dest,dest_st,orig, orig_st\n" +
            "MATCH\n" +
            "    p = allShortestPaths((orig_st)-[*]->(dest_st))\n" +
            "WITH\n" +
            "    nodes(p) AS n\n" +
            "UNWIND\n" +
            "    n AS nodes\n" +
            "//MATCH\n" +
            "//  p=((nodes)-[loc:LOCATED_AT]->(stp:Stop))\n" +
            "OPTIONAL MATCH\n" +
            "  p=(nodes)-[r:PRECEDES|LOCATED_AT]->(next)\n" +
            "RETURN\n" +
            "    p, //nodes.stop_sequence AS stopSequence \n" +
            "    nodes.departure_time_int AS departureTimeInt \n"
            )
    ArrayList<Stoptime> getMyTrips(Sort sort);

}


