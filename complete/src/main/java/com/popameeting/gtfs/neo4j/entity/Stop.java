package com.popameeting.gtfs.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
/**
 * Created by tgulesserian on 5/18/17.
 */
@Node
@Data
public class Stop {
    @Id
    private Long id;

    @Property(name="name")
    private String name;

    @Property(name="lon")
    private double longitude;

    @Property(name="lat")
    private double latitude;

    @Property(name="id")
    private String stopId;

}
