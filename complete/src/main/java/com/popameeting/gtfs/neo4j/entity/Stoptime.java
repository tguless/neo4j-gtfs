package com.popameeting.gtfs.neo4j.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@NodeEntity
public class Stoptime {
    @GraphId
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

    @Relationship(type = "LOCATED_AT", direction = Relationship.OUTGOING)
    private Set<Stop> stops;

    @Relationship(type = "PART_OF_TRIP")
    public Set<Trip> trips;

    @Relationship(type = "PRECEDES")
    public Set<Stoptime> precedesTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public int getDepartureTimeInt() {
        return departureTimeInt;
    }

    public void setDepartureTimeInt(int departureTimeInt) {
        this.departureTimeInt = departureTimeInt;
    }

    public int getArrivalTimeInt() {
        return arrivalTimeInt;
    }

    public void setArrivalTimeInt(int arrivalTimeInt) {
        this.arrivalTimeInt = arrivalTimeInt;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public Set<Stop> getStops() {
        return stops;
    }

    public void setStops(Set<Stop> stops) {
        this.stops = stops;
    }

    public Set<Trip> getTrips() {
        return trips;
    }

    public void setTrips(Set<Trip> trips) {
        this.trips = trips;
    }

    public Set<Stoptime> getPrecedesTime() {
        return precedesTime;
    }

    public void setPrecedesTime(Set<Stoptime> precedesTime) {
        this.precedesTime = precedesTime;
    }
}
