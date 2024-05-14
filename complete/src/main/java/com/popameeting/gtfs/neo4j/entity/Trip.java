package com.popameeting.gtfs.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@Node
@Data
public class Trip {

    @Id
    private Long id;

    @Property(name="id")
    private String tripId;

    @Property(name="service_id")
    private String serviceId;

    @Relationship(type = "USES")
    public Set<Route> routes;

    @Relationship(type = "PART_OF_TRIP", direction = Relationship.Direction.INCOMING)
    public Set<Stoptime> stoptimes;

    @Relationship(type = "RUNS_DURING")
    public Set<CalendarDate> calendarDates;

}
