package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.CalendarDate;
import com.popameeting.gtfs.neo4j.entity.Stoptime;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by tgulesserian on 5/26/17.
 */
@RepositoryRestResource(collectionResourceRel = "calendarDate", path = "calendarDate")
public interface CalendarDateRepository extends Neo4jRepository<CalendarDate, Long> {
    @Query(
            "LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/calendar_dates.txt' AS csv\n" +
            "MATCH (t:Trip {service_id: csv.service_id})\n" +
            "CREATE (t)-[:RUNS_DURING]->(cd:CalendarDate{service_id: csv.service_id, date: csv.date, exception_type: csv.exception_type })"
    )
    void loadNodes ();
}
