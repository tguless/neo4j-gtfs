package com.popameeting.gtfs.neo4j.entity;



import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.Set;

/**
 * Created by tgulesserian on 5/26/17.
 */
@Node
@Data
public class CalendarDate {
    @Id
    private Long id;

    @Property(name="service_id")
    private String serviceId;

    @Property(name="date")
    private String date;

    @Property(name="exception_type")
    private String exceptionType;

    @Relationship(type = "RUNS_DURING", direction = Relationship.Direction.INCOMING)
    public Set<Trip> trips;

}
