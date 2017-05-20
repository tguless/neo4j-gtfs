package com.popameeting.gtfs.neo4j.entity.projection.stop;

import com.popameeting.gtfs.neo4j.entity.Stop;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by tgulesserian on 5/19/17.
 */
@Projection(name = "StopName", types = { Stop.class })
public interface StopNamePjcn {
    public String getName();
}
