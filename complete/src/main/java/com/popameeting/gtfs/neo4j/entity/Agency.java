package com.popameeting.gtfs.neo4j.entity;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@NodeEntity
public class Agency {
    @GraphId
    private Long id;

    @Property(name="id")
    private String agencyId;

    @Property(name="name")
    private String name;

    @Property(name="url")
    private String url;

    @Property(name="timezone")
    private String timezone;

    @Relationship(type = "OPERATES", direction = Relationship.OUTGOING)
    public Set<Route> routes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


}
