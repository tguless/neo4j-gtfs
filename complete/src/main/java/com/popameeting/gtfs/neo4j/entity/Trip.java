package com.popameeting.gtfs.neo4j.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@NodeEntity
public class Trip {

    @GraphId
    private Long id;

    @Property(name="id")
    private String tripId;

    @Property(name="service_id")
    private String serviceId;

    @Relationship(type = "USES")
    public Set<Route> routes;

    @Relationship(type = "PART_OF_TRIP", direction = Relationship.INCOMING)
    public Set<Stoptime> stoptimes;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<Route> routes) {
        this.routes = routes;
    }


    public Set<Stoptime> getStoptimes() {
        return stoptimes;
    }

    public void setStoptimes(Set<Stoptime> stoptimes) {
        this.stoptimes = stoptimes;
    }

}
