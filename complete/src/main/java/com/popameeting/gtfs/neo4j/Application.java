package com.popameeting.gtfs.neo4j;



import com.popameeting.gtfs.neo4j.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories
public class Application {
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


    private final static Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

    /*
	@Bean
	CommandLineRunner demo(PersonRepository personRepository, NjTransitGtfsService svc) {
		return args -> {

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



			personRepository.deleteAll();

			Person greg = new Person("Greg");
			Person roy = new Person("Roy");
			Person craig = new Person("Craig");

			List<Person> team = Arrays.asList(greg, roy, craig);

			log.info("Before linking up with Neo4j...");

			team.stream().forEach(person -> log.info("\t" + person.toString()));

			personRepository.save(greg);
			personRepository.save(roy);
			personRepository.save(craig);

			greg = personRepository.findByName(greg.getName());
			greg.worksWith(roy);
			greg.worksWith(craig);
			personRepository.save(greg);

			roy = personRepository.findByName(roy.getName());
			roy.worksWith(craig);
			// We already know that roy works with greg
			personRepository.save(roy);

			// We already know craig works with roy and greg

			log.info("Lookup each person by name...");
			team.stream().forEach(person -> log.info(
					"\t" + personRepository.findByName(person.getName()).toString()));

		};
	}
	*/

}
