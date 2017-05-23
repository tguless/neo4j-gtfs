package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.dto.TripPlan;
import com.popameeting.gtfs.neo4j.entity.Stoptime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

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
        coming back correct. I see it making into the cypher query - but JSON I get back has the first two records in the
        wrong order, but the rest are ok.
        http://localhost:8080/stoptimes/search/getMyTrips?serviceId=4&origStation=WESTWOOD&origArrivalTimeLow=06:30:00&origArrivalTimeHigh=07:10:00&destStation=HOBOKEN&destArrivalTimeLow=07:00:00&destArrivalTimeHigh=08:00:00&sort=stopSequence,asc
        http://localhost:8080/stoptimes/search/getMyTrips?serviceId=4&origStation=WESTWOOD&origArrivalTimeLow=06:30:00&origArrivalTimeHigh=07:10:00&destStation=HOBOKEN&destArrivalTimeLow=07:00:00&destArrivalTimeHigh=08:00:00&sort=departureTimeInt,asc
        With projection:
        http://localhost:8080/stoptimes/search/getMyTrips?serviceId=4&origStation=WESTWOOD&origArrivalTimeLow=06:30:00&origArrivalTimeHigh=07:10:00&destStation=HOBOKEN&destArrivalTimeLow=07:00:00&destArrivalTimeHigh=08:00:00&sort=departureTimeInt,asc&projection=TripPlanResult
    */
    @Query("//find a DIRECT route with range conditions\n" +
            "MATCH\n" +
            "  (orig:Stop {name: {origStation}})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)\n" +
            "WHERE\n"+
            "  orig_st.departure_time > {origArrivalTimeLow}\n" +
            "  AND orig_st.departure_time < {origArrivalTimeHigh}\n" +
            "  AND trp.service_id={serviceId}\n" +
            "WITH\n"+
            "  orig, orig_st\n" +
            "MATCH\n" +
            "    (dest:Stop {name: {destStation}})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)\n" +
            "WHERE\n"+
            "    dest_st.arrival_time < {destArrivalTimeHigh}\n" +
            "    AND dest_st.arrival_time > {destArrivalTimeLow}\n" +
            "    AND dest_st.arrival_time > orig_st.departure_time\n"+
            "    AND trp2.service_id={serviceId}\n" +
            "WITH\n"+
            "    dest,dest_st,orig, orig_st\n" +
            "MATCH\n" +
            "    p = allShortestPaths((orig_st)-[*]->(dest_st))\n" +
            "WITH\n" +
            "    nodes(p) AS n\n" +
            "UNWIND\n" +
            "    n AS stoptimes\n" +
            "MATCH\n" +
            "    p2=(stoptimes)-[r2:PART_OF_TRIP]->(trip)\n" +
            "MATCH\n" +
            "    p=(stoptimes)-[r:LOCATED_AT]->(stop)\n" +
            "RETURN\n" +
            "    p, p2,\n" +
            "   stoptimes.departure_time_int AS departureTimeInt, \n" +
            "   trip.id AS tripId"
            )
    Page<Stoptime> getMyTrips(
                              @Param("serviceId") String serviceId,
                              @Param("origStation") String origStation,
                              @Param("origArrivalTimeLow") String origArrivalTimeLow,
                              @Param("origArrivalTimeHigh") String origArrivalTimeHigh,
                              @Param("destStation") String destStation,
                              @Param("destArrivalTimeLow") String destArrivalTimeLow,
                              @Param("destArrivalTimeHigh")String destArrivalTimeHigh,
                              Pageable pageRequest);

    @Query("MATCH\n" +
            "    p3=(orig:Stop {name:{origStation}})--(st_orig:Stoptime)-[r1:PART_OF_TRIP]->(trp1:Trip),\n" +
            "    p4=(dest:Stop {name:{destStation}})--(st_dest:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip),\n" +
            "    p1=((st_orig)-[:PRECEDES*]->(st_midway_arr:Stoptime)),\n"+
            "    p5=(st_midway_arr)--(midway:Stop)--(st_midway_dep:Stoptime),\n" +
            "    p2=((st_midway_dep)-[:PRECEDES*]->(st_dest))\n" +
            "WHERE\n" +
            "    st_orig.departure_time > {origArrivalTimeLow}\n" +
            "    AND st_orig.departure_time < {origArrivalTimeHigh}\n" +
            "    AND st_dest.arrival_time < {destArrivalTimeHigh}\n" +
            "    AND st_dest.arrival_time > {destArrivalTimeLow}\n" +
            "    AND st_midway_arr.arrival_time > st_orig.departure_time\n" +
            "    AND st_midway_dep.departure_time > st_midway_arr.arrival_time\n" +
            "    AND st_dest.arrival_time > st_midway_dep.departure_time\n" +
            "    AND trp1.service_id = {serviceId}\n" +
            "    AND trp2.service_id = {serviceId}\n" +
            "RETURN\n" +
            "    p3,p4, p5, p1,p2,midway\n" +
            "ORDER BY\n" +
            "    (st_dest.arrival_time_int-st_orig.departure_time_int) ASC\n" +
            "SKIP {skip} LIMIT 1")
    Page<Stoptime> getMyTripsOneStop(
                                        @Param("serviceId") String serviceId,
                                        @Param("origStation") String origStation,
                                        @Param("origArrivalTimeLow") String origArrivalTimeLow,
                                        @Param("origArrivalTimeHigh") String origArrivalTimeHigh,
                                        @Param("destStation") String destStation,
                                        @Param("destArrivalTimeLow") String destArrivalTimeLow,
                                        @Param("destArrivalTimeHigh")String destArrivalTimeHigh,
                                        @Param("skip")Long skip
                                    );

    @Query("MATCH\n"+
          "  (orig:Stop {name: 'WESTWOOD'})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)\n"+
          "WHERE\n"+
          "  orig_st.departure_time > '06:30:00'\n"+
          "  AND orig_st.departure_time < '07:30:00'\n"+
          "  AND trp.service_id='4'\n"+
          "WITH\n"+
          "  orig, orig_st\n"+
          "MATCH\n"+
          "  (dest:Stop {name:'HOBOKEN'})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)\n"+
          "  WHERE\n"+
          "  dest_st.arrival_time < '09:00:00'\n"+
          "  AND dest_st.arrival_time > '07:00:00'\n"+
          "  AND dest_st.arrival_time > orig_st.departure_time\n"+
          "  AND trp2.service_id='4'\n"+
          "WITH\n"+
          "  dest,dest_st,orig, orig_st, trp2\n"+
          "MATCH\n"+
          "  p = allshortestpaths((orig_st)-[*]->(dest_st))\n"+
          "RETURN\n"+
          "  p")
    Set<Stoptime> getMyStops();

    /* No Spring Expression Language Support for Neo4j yet
    https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions

    @Query("//find a DIRECT route with range conditions\n" +
            "MATCH\n" +
            "  (orig:Stop {name: {#{#tripPlan.origStation}}})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)\n" +
            "WHERE\n"+
            "  orig_st.departure_time > {#{#tripPlan.origArrivalTimeLow}}\n" +
            "  AND orig_st.departure_time < {#{#tripPlan.origArrivalTimeLow}}\n" +
            "  AND trp.service_id={#{#tripPlan.serviceId}}\n" +
            "WITH\n"+
            "  orig, orig_st\n" +
            "MATCH\n" +
            "    (dest:Stop {name: {#{#tripPlan.destStation}}})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)\n" +
            "WHERE\n"+
            "    dest_st.arrival_time < {#{#tripPlan.destArrivalTimeHigh}}\n" +
            "    AND dest_st.arrival_time > {#{#tripPlan.destArrivalTimeLow}}\n" +
            "    AND dest_st.arrival_time > orig_st.departure_time\n"+
            "    AND trp2.service_id={#{#tripPlan.serviceId}}\n" +
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
    Page<Stoptime> getMyTrips2(
            //ArrayList<Stoptime> getMyTrips(
            @Param("tripPlan") TripPlan tripPlan,
            Pageable pageRequest);
    */

}


