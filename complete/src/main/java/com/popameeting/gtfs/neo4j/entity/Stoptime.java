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
public class Stoptime {
    @Id
    private Long id;

    @Property(name="arrival_time")
    private String arrivalTime;

    @Property(name="stop_sequence")
    private int stopSequence;

    @Property(name="departure_time_int")
    private int departureTimeInt;

    @Property(name="arrival_time_int")
    private int arrivalTimeInt;

    @Property(name="departure_time")
    private String departureTime;

    @Relationship(type = "LOCATED_AT", direction = Relationship.Direction.OUTGOING)
    private Set<Stop> stops;

    @Relationship(type = "PART_OF_TRIP")
    public Set<Trip> trips;

    @Relationship(type = "PRECEDES")
    public Set<Stoptime> precedesTime;

}
