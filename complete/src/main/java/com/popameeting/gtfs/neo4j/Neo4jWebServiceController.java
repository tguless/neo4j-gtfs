package com.popameeting.gtfs.neo4j;

import com.popameeting.gtfs.neo4j.dto.TripPlan;
import com.popameeting.gtfs.neo4j.entity.*;
import com.popameeting.gtfs.neo4j.entity.projection.stoptime.TripPlanResultPjcn;
import com.popameeting.gtfs.neo4j.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Created by tgulesserian on 5/18/17.
 */
@Controller
@RequestMapping("/customrest")
public class Neo4jWebServiceController {
    @Autowired
    AgencyRepository agencyRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    StopRepository stopRepository;

    @Autowired
    StoptimeRepository stoptimeRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    NjTransitGtfsService svc;


    @Autowired
    ProjectionFactory projectionFactory;


    @GetMapping(path = "/agency/{agencyId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example id: NJT
    public Agency getAgency(@PathVariable String agencyId, Model model) {
        return agencyRepository.findByAgencyId(agencyId,1);
    }

    @GetMapping(path = "/agency/{agencyId}/routes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example id: NJT
    public Set<Route> getAgencyRoutes(@PathVariable String agencyId, Model model) {
        Agency agency = agencyRepository.findByAgencyId(agencyId,1);
        return agency.routes;
    }

    @GetMapping(path = "/route/{routeId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example id: 13
    public Route getRoute(@PathVariable String routeId, Model model) {
        return routeRepository.findByRouteId(routeId,1);
    }

    @GetMapping(path = "/stop/{stopName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example name: WESTWOOD
    public Stop getStop(@PathVariable String stopName, Model model) {
        return stopRepository.findByName(stopName,1);
    }

    @GetMapping(path = "/stoptime/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example id: 1270015
    public Stoptime getStopTime(@PathVariable Long id, Model model) {
        stoptimeRepository.findOne(id,1);
        return null;
    }

    @GetMapping(path = "/trip/{tripId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    //Example id: 22
    public Trip getTrip(@PathVariable String tripId, Model model) {
        return tripRepository.findByTripId(tripId, 1);

    }

    @RequestMapping(value = "/LoadData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String grabAndLoad() {
        svc.downloadtGtfs();
        loadFetchedZip();
        return "done";
    }

    @RequestMapping(value = "/plantrip", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<?> planTrip2( @RequestBody TripPlan plan, Pageable pageable){

        /* No Spring Expression Language (SpEL) support yet in for spring data Neo4j
        https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions

        Page<?> test= stoptimeRepository.getMyTrips2(
                plan,
                pageable).//
                map(stoptime -> projectionFactory.createProjection(TripPlanResultPjcn.class, stoptime));
        */

        if(pageable.getSort() == null || !pageable.getSort().iterator().hasNext()) {
            Sort sort = new Sort(Sort.Direction.ASC, "departureTimeInt");
            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        Page<?> test= stoptimeRepository.getMyTrips(
                plan.getServiceId(),
                plan.getOrigStation(),
                plan.getOrigArrivalTimeLow(),
                plan.getOrigArrivalTimeHigh(),
                plan.getDestStation(),
                plan.getDestArrivalTimeLow(),
                plan.getDestArrivalTimeHigh(),
                pageable).//
                map(stoptime -> projectionFactory.createProjection(TripPlanResultPjcn.class, stoptime));

        return test;

    }

    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<?> planTrip(Pageable pageable) {

        //Sort sort = new Sort(Sort.Direction.ASC, "departureTimeInt");

        if(pageable.getSort() == null || !pageable.getSort().iterator().hasNext()) {
            Sort sort = new Sort(Sort.Direction.ASC, "tripId").
                        and( new Sort(Sort.Direction.ASC, "departureTimeInt"));
            pageable = new PageRequest(pageable.getPageNumber(), 500, sort);
        }

        Page<?> test= stoptimeRepository.getMyTrips(
                "4",
                "WESTWOOD",
                "06:30:00",
                "07:30:00",
                "HOBOKEN",
                "07:00:00",
                "09:00:00",
                pageable).//
                map(stoptime -> projectionFactory.createProjection(TripPlanResultPjcn.class, stoptime));
        return test;

    }


    @RequestMapping(value = "/LoadPrefetched", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String loadFetchedZip() {

        svc.unzipGtfsZip();

        agencyRepository.deleteAll();
        routeRepository.deleteAll();
        stopRepository.deleteAll();
        stoptimeRepository.deleteAll();
        tripRepository.deleteAll();

        agencyRepository.loadNodes();
        routeRepository.loadNodes();
        tripRepository.loadNodes();

        stopRepository.addStops();
        stopRepository.connectParentChild();

        stoptimeRepository.addStopTimes();
        stoptimeRepository.stopTimeToInt();
        stoptimeRepository.connectSequences();

        return "done";

    }

}
