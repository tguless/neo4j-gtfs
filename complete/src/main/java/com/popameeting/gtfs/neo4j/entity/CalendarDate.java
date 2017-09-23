package com.popameeting.gtfs.neo4j.entity;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/26/17.
 */
@NodeEntity
public class CalendarDate {
    @GraphId
    private Long id;

    @Property(name="service_id")
    private String serviceId;

    @Property(name="date")
    private String date;

    @Property(name="exception_type")
    private String exceptionType;

    @Relationship(type = "RUNS_DURING", direction = Relationship.INCOMING)
    public Set<Trip> trips;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Set<Trip> getTrips() {
        return trips;
    }

    public void setTrips(Set<Trip> trips) {
        this.trips = trips;
    }
}
