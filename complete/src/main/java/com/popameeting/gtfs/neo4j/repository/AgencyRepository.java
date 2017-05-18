package com.popameeting.gtfs.neo4j.repository;

import com.popameeting.gtfs.neo4j.entity.Agency;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by tgulesserian on 5/18/17.
 */
public interface AgencyRepository extends GraphRepository<Agency>,Importable {
    @Query("LOAD CSV WITH HEADERS FROM\n" +
            "'file:///nmbs/agency.txt' AS csv\n" +
            "CREATE (a:Agency {id: csv.agency_id, name: csv.agency_name, url: csv.agency_url, timezone: csv.agency_timezone});\n")
    void loadNodes ();

}
