package com.popameeting.gtfs.neo4j.entity;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Created by tgulesserian on 5/18/17.
 */
@NodeEntity
public class Stoptime {
    @GraphId
    private Long id;
}
