package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.dto.TripPlan;
import com.popameeting.gtfs.neo4j.entity.Stoptime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface StoptimeRepository extends Neo4jRepository<Stoptime, Long> {


    @Query("//add the stoptimes \n" +
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/stop_times.txt' AS csv\n" +
            "MATCH (t:Trip {id: csv.trip_id}), (s:Stop {id: csv.stop_id})\n" +
            "CREATE (t)<-[:PART_OF_TRIP]-(st:Stoptime {arrival_time: csv.arrival_time, departure_time: csv.departure_time, stop_sequence: toInteger(csv.stop_sequence)})-[:LOCATED_AT]->(s);\n")
    void addStopTimes();

    @Query( "//create integers out of the stoptimes (to allow for calculations/ordering)\n" +
            "MATCH (s:Stoptime)\n" +
            "SET s.arrival_time_int=toInteger(replace(s.arrival_time,':',''))/100\n" +
            "SET s.departure_time_int=toInteger(replace(s.departure_time,':',''))/100\n" +
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
    @Query(
            value="""
                    MATCH
                      (cd:CalendarDate)
                    WHERE 
                       cd.date = $travelDate AND
                       cd.exception_type = '1'
                    WITH 
                       cd
                    MATCH
                      (orig:Stop {name: $origStation})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)
                    WHERE
                      orig_st.departure_time > $origArrivalTimeLow AND
                      orig_st.departure_time < $origArrivalTimeHigh AND
                      trp.service_id=cd.service_id
                    WITH    
                      orig, orig_st, cd
                    MATCH
                        (dest:Stop {name: $destStation})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)
                    WHERE
                        dest_st.arrival_time < $destArrivalTimeHigh AND
                        dest_st.arrival_time > $destArrivalTimeLow AND
                        dest_st.arrival_time > orig_st.departure_time AND
                        trp2.service_id=cd.service_id
                    WITH
                        dest,dest_st,orig, orig_st
                    MATCH
                        p = allShortestPaths((orig_st)-[*]->(dest_st))
                    WITH
                        nodes(p) AS n
                    UNWIND
                        n AS stoptimes
                    MATCH
                        p2=(stoptimes)-[r2:PART_OF_TRIP]->(trip)
                    MATCH
                        p=(stoptimes)-[r:LOCATED_AT]->(stop)
                    RETURN
                        stoptimes, trip, stop, r, r2, p, p2,
                        stoptimes.departure_time_int AS departureTimeInt, 
                        trip.id AS tripId
            """,

            countQuery = """
                    MATCH
                      (cd:CalendarDate)
                    WHERE
                       cd.date = $travelDate AND
                       cd.exception_type = '1'
                    WITH
                       cd
                    MATCH
                      (orig:Stop {name: $origStation})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)
                    WHERE
                      orig_st.departure_time > $origArrivalTimeLow AND
                      orig_st.departure_time < $origArrivalTimeHigh AND
                      trp.service_id=cd.service_id
                    WITH
                      orig, orig_st, cd
                    MATCH
                        (dest:Stop {name: $destStation})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)
                    WHERE
                        dest_st.arrival_time < $destArrivalTimeHigh AND
                        dest_st.arrival_time > $destArrivalTimeLow AND
                        dest_st.arrival_time > orig_st.departure_time AND
                        trp2.service_id=cd.service_id
                    WITH
                        dest,dest_st,orig, orig_st
                    MATCH
                        p = allShortestPaths((orig_st)-[*]->(dest_st))
                    WITH
                        nodes(p) AS n
                    UNWIND
                        n AS stoptimes
                    MATCH
                        p2=(stoptimes)-[r2:PART_OF_TRIP]->(trip)
                    MATCH
                        p=(stoptimes)-[r:LOCATED_AT]->(stop)
                    RETURN
                       count(trip) as count
            """
            )
    <T> Page<T> getMyTrips(
                              @Param("travelDate") String travelDate,
                              @Param("origStation") String origStation,
                              @Param("origArrivalTimeLow") String origArrivalTimeLow,
                              @Param("origArrivalTimeHigh") String origArrivalTimeHigh,
                              @Param("destStation") String destStation,
                              @Param("destArrivalTimeLow") String destArrivalTimeLow,
                              @Param("destArrivalTimeHigh")String destArrivalTimeHigh,
                              Pageable pageRequest,
                              Class<T> type);

    @Query(
            "MATCH\n" +
            "  (cd:CalendarDate)\n" +
            "WHERE \n" +
            "    cd.date = $travelDate AND \n" +
            "    cd.exception_type = '1'\n" +
            "WITH cd\n" +
            "MATCH\n" +
            "    p3=(orig:Stop {name: $origStation})<-[:LOCATED_AT]-(st_orig:Stoptime)-[r1:PART_OF_TRIP]->(trp1:Trip),\n" +
            "    p4=(dest:Stop {name:$destStation})<-[:LOCATED_AT]-(st_dest:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip),\n" +
            "    p1=(st_orig)-[im1:PRECEDES*]->(st_midway_arr:Stoptime),\n"+
            "    p5=(st_midway_arr)-[:LOCATED_AT]->(midway:Stop)<-[:LOCATED_AT]-(st_midway_dep:Stoptime),\n" +
            "    p2=(st_midway_dep)-[im2:PRECEDES*]->(st_dest)\n" +
            "WHERE\n" +
            "  st_orig.departure_time > $origArrivalTimeLow\n" +
            "  AND st_orig.departure_time < $origArrivalTimeHigh\n" +
            "  AND st_dest.arrival_time < $destArrivalTimeHigh\n" +
            "  AND st_dest.arrival_time > $destArrivalTimeLow\n" +
            "  AND st_midway_arr.arrival_time > st_orig.departure_time\n"+
            "  AND st_midway_dep.departure_time > st_midway_arr.arrival_time\n" +
            "  AND st_dest.arrival_time > st_midway_dep.departure_time\n" +
            "  AND trp1.service_id = cd.service_id\n" +
            "  AND trp2.service_id = cd.service_id\n" +
            "WITH\n"+
            "  st_orig, st_dest, nodes(p1) + nodes(p2) AS allStops1\n" +
            "ORDER BY\n" +
            "    (st_dest.arrival_time_int-st_orig.departure_time_int) ASC\n" +
            "SKIP $skip LIMIT 1\n" +
            "UNWIND\n" +
            "  allStops1 AS stoptime\n" +
            "MATCH\n" +
            "  p6=(loc:Stop)<-[r:LOCATED_AT]-(stoptime)-[r2:PART_OF_TRIP]->(trp5:Trip),\n" +
            "  (stoptime)-[im1:PRECEDES*]->(stoptime2)\n" +
            "RETURN\n" +
            "  p6\n" +
            "ORDER BY stoptime.departure_time_int ASC\n" +
            ";")
    <T> List<T> getMyTripsOneStop(
                                        @Param("travelDate") String travelDate,
                                        @Param("origStation") String origStation,
                                        @Param("origArrivalTimeLow") String origArrivalTimeLow,
                                        @Param("origArrivalTimeHigh") String origArrivalTimeHigh,
                                        @Param("destStation") String destStation,
                                        @Param("destArrivalTimeLow") String destArrivalTimeLow,
                                        @Param("destArrivalTimeHigh")String destArrivalTimeHigh,
                                        @Param("skip")Long skip,
                                        Class<T> type
                                    );

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

    @Query("MATCH (s:Stoptime)-[*1..$depth]-(related) WHERE id(s) = $id RETURN s, collect(related)")
    Optional<Stoptime> findByIdWithDepth(@Param("id") Long id, @Param("depth") int depth);

}


