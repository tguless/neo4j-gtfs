package com.popameeting.gtfs.neo4j;


import com.popameeting.gtfs.neo4j.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * Created by tgulesserian on 5/18/17.
 */
@Controller
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

    @RequestMapping(value = "/LoadData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStatus() {

        svc.grabGtfs();
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
