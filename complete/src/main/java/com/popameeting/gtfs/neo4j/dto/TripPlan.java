package com.popameeting.gtfs.neo4j.dto;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Created by tgulesserian on 5/20/17.
 */
@Data
public class TripPlan {
    private String travelDate;
    private String origStation;
    private String origArrivalTimeLow;
    private String origArrivalTimeHigh;
    private String destStation;
    private String destArrivalTimeLow;
    private String destArrivalTimeHigh;
}
