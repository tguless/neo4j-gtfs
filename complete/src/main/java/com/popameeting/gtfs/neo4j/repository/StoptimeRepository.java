package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Stoptime;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface StoptimeRepository extends GraphRepository<Stoptime> {


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
            "match (s1:Stoptime)-[:PART_OF_TRIP]->(t:Trip),\n" +
            "      (s2:Stoptime)-[:PART_OF_TRIP]->(t)\n" +
            "  where s2.stop_sequence=s1.stop_sequence + 1 \n" +
            "create (s1)-[:PRECEDES]->(s2);")
    void connectSequences();


}


