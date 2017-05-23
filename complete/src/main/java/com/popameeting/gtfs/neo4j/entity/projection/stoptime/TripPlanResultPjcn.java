package com.popameeting.gtfs.neo4j.entity.projection.stoptime;

import com.popameeting.gtfs.neo4j.entity.Stoptime;
import com.popameeting.gtfs.neo4j.entity.projection.stop.StopNamePjcn;
import com.popameeting.gtfs.neo4j.entity.projection.trip.TripNoBackrefsPjcn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.Set;

/**
 * Created by tgulesserian on 5/19/17.
 */
@Projection(name = "TripPlanResult", types = { Stoptime.class })
public interface TripPlanResultPjcn {

    public String getArrivalTime();
    public String getDepartureTime();
    public int getStopSequence();

    @Value("#{target.stops.iterator().next().getName()}")
    public String getStopName();

    //public Set<StopNamePjcn> getStops();

    @Value("#{target.trips.iterator().next().getTripId()}")
    public String getTripId();

    //public Set<TripNoBackrefsPjcn> getTrips();
}
