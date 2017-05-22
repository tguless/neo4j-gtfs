package com.popameeting.gtfs.neo4j.entity.projection.trip;

import com.popameeting.gtfs.neo4j.entity.Trip;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by tgulesserian on 5/21/17.
 */
@Projection(name = "TripNoBackrefs", types = { Trip.class })
public interface TripNoBackrefsPjcn {

    public String getTripId();
}
