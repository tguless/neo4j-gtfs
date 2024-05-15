package com.popameeting.gtfs.neo4j.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@Node
@Data
public class Route {
    //@Id
    //private Long id;

    @Property(name="short_name")
    private String shortName;

    @Property(name="long_name")
    private String longName;

    @Id
    @Property(name="id")
    private String routeId;

    @Property(name="type")
    private long type;

    @Relationship(type = "USES", direction = Relationship.Direction.INCOMING)
    private Set<Trip> trips;

}
