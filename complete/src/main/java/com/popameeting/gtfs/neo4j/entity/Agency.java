package com.popameeting.gtfs.neo4j.entity;


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
public class Agency {
    //@Id
    //private Long id;

    @Id
    @Property(name="id")
    private String agencyId;

    @Property(name="name")
    private String name;

    @Property(name="url")
    private String url;

    @Property(name="timezone")
    private String timezone;

    @Relationship(type = "OPERATES", direction = Relationship.Direction.OUTGOING)
    public Set<Route> routes;



}
