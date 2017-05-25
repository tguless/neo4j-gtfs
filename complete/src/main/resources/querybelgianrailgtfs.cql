//Query a GFTS dataset
//TODO: add these to the Spring Data Repository API
//http://docs.spring.io/spring-data/neo4j/docs/current/reference/html/

//Find ANTWERP
MATCH (s:Stop)
WHERE upper(s.name) STARTS WITH "ANTW"
RETURN s;

//Find Turnhout part 1
MATCH (s:Stop)
WHERE s.name STARTS WITH "Turn"
RETURN s;

//Find Turnhout part 2
MATCH (s:Stop)
WHERE upper(s.name) STARTS WITH "TURN"
RETURN s;

//Find Antwerp and Turnhout
// match (s:Stop)
// where s.name starts with "Antw"
// or s.name starts with "Turn"
// return s;

MATCH (ant:Stop), (tu:Stop)
WHERE ant.name START WITH "Antw"
AND tu.name STARTS WITH "Turn"
RETURN DISTINCT tu,ant;

// match (ant:Stop)
// where ant.name starts with "Antw"
// return ant.name as name
// union
// match (tu:Stop)
// where tu.name starts with "Turn"
// return tu.name as name;

//find the links between the top-level stops
MATCH
    (t:Stop)<-[:PART_OF]-(:Stop),
    (a:Stop)<-[:PART_OF]-(:Stop)
WHERE
    t.name starts with "Turn"
AND
    a.name="Antwerpen-Centraal"
with
    t,a
MATCH
    p = allshortestpaths((t)-[*]-(a))
RETURN
    p
LIMIT
    10;

MATCH
    (t:Stop),(a:Stop)
WHERE
    t.name starts with "Turn"
    AND a.name="Antwerpen-Centraal"
WITH
    t,a
MATCH
    p = allshortestpaths((t)-[*]-(a))
RETURN
    p
LIMIT
    10
;

//range queries with and without indexes
MATCH (st:Stoptime)
WHERE st.departure_time < "07:45:00"
RETURN st.departure_time;

MATCH
  (orig:Stop {name: "WESTWOOD"})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)
  WHERE
  orig_st.departure_time > "07:00:00"
  AND orig_st.departure_time < "09:00:00"
  AND trp.service_id="4"
return orig, orig_st
;

//find a DIRECT route with range conditions
MATCH
  (orig:Stop {name: "WESTWOOD"})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)
WHERE
  orig_st.departure_time > "06:30:00"
  AND orig_st.departure_time < "07:30:00"
  AND trp.service_id="4"
WITH
  orig, orig_st
MATCH
    (dest:Stop {name:"HOBOKEN"})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)
WHERE
    dest_st.arrival_time < "09:00:00"
    AND dest_st.arrival_time > "07:00:00"
    AND dest_st.arrival_time > orig_st.departure_time
    AND trp2.service_id="4"
WITH
    dest,dest_st,orig, orig_st, trp2
MATCH
    p = allshortestpaths((orig_st)-[*]->(dest_st))
WITH
    nodes(p) as n
UNWIND
    n as stoptime
//MATCH
//  p=((nodes)-[loc:LOCATED_AT]->(stp:Stop))
MATCH
    p2=(stoptime)-[r2:PART_OF_TRIP]->(trip)
MATCH
    p=(stoptime)-[r:LOCATED_AT]->(stop)
    //p=(nodes)-[r:LOCATED_AT]->(next)
RETURN
    p, p2 //, COALESCE(nodes.stop_sequence, next.stop_sequence) AS stopSequence // nodes, r, next, loc,stp
ORDER BY trip.trip_id, stoptime.departure_time_int;



//find a DIRECT route with range conditions
MATCH
  (orig:Stop {name: "WESTWOOD"})--(orig_st:Stoptime)-[r1:PART_OF_TRIP]->(trp:Trip)
  WHERE
  orig_st.departure_time > "06:30:00"
  AND orig_st.departure_time < "07:30:00"
  AND trp.service_id="4"
WITH
  orig, orig_st
MATCH
  (dest:Stop {name:"HOBOKEN"})--(dest_st:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip)
  WHERE
  dest_st.arrival_time < "09:00:00"
  AND dest_st.arrival_time > "07:00:00"
  AND dest_st.arrival_time > orig_st.departure_time
  AND trp2.service_id="4"
WITH
  dest,dest_st,orig, orig_st, trp2
MATCH
  p = allshortestpaths((orig_st)-[*]->(dest_st))
RETURN
  p

//Indirect routes

//find the route and the stops for the indirect route
MATCH
    (t:Stop),(a:Stop)
WHERE
    t.name = "WESTWOOD"
AND
    a.name="HOBOKEN"
WITH
    t,a
MATCH
    p = allshortestpaths((t)-[*]-(a))
WHERE
    NONE (x in relationships(p) where type(x)="OPERATES")
RETURN
    p
LIMIT
    10;


// plan a specific route with one stop
MATCH
  (orig:Stop {name:"WESTWOOD"})--(st_orig:Stoptime),
  (dest:Stop {name:"RUTHERFORD"})--(st_dest:Stoptime),
  p1=(st_orig)-[:PRECEDES*]->(st_midway_arr:Stoptime),
  (st_midway_arr)--(midway:Stop),
  (midway)--(st_midway_dep:Stoptime),
  p2=(st_midway_dep)-[:PRECEDES*]->(st_dest)
WHERE
  st_orig.departure_time > "08:00:00"
  AND st_orig.departure_time < "11:00:00"
  AND st_midway_arr.arrival_time > st_orig.departure_time
  AND st_midway_dep.departure_time > st_midway_arr.arrival_time
  AND st_dest.arrival_time > st_midway_dep.departure_time
RETURN
  orig,st_orig, midway, st_midway_arr, st_midway_dep, dest, st_dest
ORDER BY
  (st_dest.arrival_time_int-st_orig.departure_time_int) ASC
LIMIT 1;

//plan a specific indirect route full details
MATCH
    p3=(orig:Stop {name:"WESTWOOD"})<-[:LOCATED_AT]-(st_orig:Stoptime)-[r1:PART_OF_TRIP]->(trp1:Trip),
    p4=(dest:Stop {name:"RUTHERFORD"})<-[:LOCATED_AT]-(st_dest:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip),
    p1=(st_orig)-[im1:PRECEDES*]->(st_midway_arr:Stoptime),
    p5=(st_midway_arr)-[:LOCATED_AT]->(midway:Stop)<-[:LOCATED_AT]-(st_midway_dep:Stoptime),
    p2=(st_midway_dep)-[im2:PRECEDES*]->(st_dest)
WHERE
  st_orig.departure_time > "07:30:00"
  AND st_orig.departure_time < "09:00:00"
  AND st_dest.arrival_time < "10:30:00"
  AND st_dest.arrival_time > "09:00:00"
  AND st_midway_arr.arrival_time > st_orig.departure_time
  AND st_midway_dep.departure_time > st_midway_arr.arrival_time
  AND st_dest.arrival_time > st_midway_dep.departure_time
  AND trp1.service_id = "4"
  AND trp2.service_id = "4"
WITH
  st_orig, st_dest, nodes(p1) + nodes(p2) AS allStops1
ORDER BY
    (st_dest.arrival_time_int-st_orig.departure_time_int) ASC
SKIP 0 LIMIT 1
UNWIND
  allStops1 AS stoptime
MATCH
  p6=(loc:Stop)<-[r:LOCATED_AT]-(stoptime)-[r2:PART_OF_TRIP]->(trp5:Trip),
  (stoptime)-[im1:PRECEDES*]->(stoptime2)
RETURN
  p6, im1
ORDER BY stoptime.departure_time_int ASC
;

MATCH (n) WHERE ID(n) = 1266412 WITH n MATCH p=(n)-[*0..1]-(m) RETURN p;

MATCH
  p3=(orig:Stop {name:"WESTWOOD"})--(st_orig:Stoptime)-[r1:PART_OF_TRIP]->(trp1:Trip),
  p4=(dest:Stop {name:"RUTHERFORD"})--(st_dest:Stoptime)-[r2:PART_OF_TRIP]->(trp2:Trip),
  p1=(st_orig)-[im1:PRECEDES*]->(st_midway_arr:Stoptime)-[r3:PART_OF_TRIP]->(trp3:Trip),
  p5=(st_midway_arr)--(midway:Stop)--(st_midway_dep:Stoptime)-[r4:PART_OF_TRIP]->(trp4:Trip),
  p2=(st_midway_dep)-[im2:PRECEDES*]->(st_dest)//,
//(:nodes(p1)) -[:PART_OF_TRIP]-> (trp5:Trip)
  WHERE
  st_orig.departure_time > "07:30:00"
  AND st_orig.departure_time < "09:00:00"
  AND st_dest.arrival_time < "10:30:00"
  AND st_dest.arrival_time > "09:00:00"
  AND st_midway_arr.arrival_time > st_orig.departure_time
  AND st_midway_dep.departure_time > st_midway_arr.arrival_time
  AND st_dest.arrival_time > st_midway_dep.departure_time
  AND trp1.service_id = "4"
  AND trp2.service_id = "4"
RETURN
  st_orig, st_dest, nodes(p1) + nodes(p2) AS allStops1 // p2 + p1 // nodes(p1) + nodes(p2) AS allStops1
ORDER BY
  (st_dest.arrival_time_int-st_orig.departure_time_int) ASC