//LOAD CSV script for GFTS data

create constraint on (a:Agency) assert a.id is unique;
create constraint on (r:Route) assert r.id is unique;
create constraint on (t:Trip) assert t.id is unique;
create index on :Trip(service_id);
create constraint on (s:Stop) assert s.id is unique;
create index on :Stoptime(stop_sequence);
create index on :Stop(name);

MATCH (n)
DETACH DELETE n;

//schema await;

//add the agency
load csv with headers from
'file:///nmbs/agency.txt' as csv
create (a:Agency {id: csv.agency_id, name: csv.agency_name, url: csv.agency_url, timezone: csv.agency_timezone});

// add the routes
load csv with headers from
'file:///nmbs/routes.txt' as csv
match (a:Agency {id: csv.agency_id})
create (a)-[:OPERATES]->(r:Route {id: csv.route_id, short_name: csv.route_short_name, long_name: csv.route_long_name, type: toInt(csv.route_type)});

// add the trips
load csv with headers from
'file:///nmbs/trips.txt' as csv
match (r:Route {id: csv.route_id})
merge (r)<-[:USES]-(t:Trip {id: csv.trip_id, service_id: csv.service_id});

//add the stops
load csv with headers from
'file:///nmbs/stops.txt' as csv
create (s:Stop {id: csv.stop_id, name: csv.stop_name, lat: toFloat(csv.stop_lat), lon: toFloat(csv.stop_lon), platform_code: csv.platform_code, parent_station: csv.parent_station, location_type: csv.location_type});

//connect parent/child relationships to stops
load csv with headers from
'file:///nmbs/stops.txt' as csv
with csv
  where not (csv.parent_station is null)
match (ps:Stop {id: csv.parent_station}), (s:Stop {id: csv.stop_id})
create (ps)<-[:PART_OF]-(s);

//add the stoptimes
load csv with headers from
'file:///nmbs/stop_times.txt' as csv
match (t:Trip {id: csv.trip_id}), (s:Stop {id: csv.stop_id})
create (t)<-[:PART_OF_TRIP]-(st:Stoptime {arrival_time: csv.arrival_time, departure_time: csv.departure_time, stop_sequence: toInt(csv.stop_sequence)})-[:LOCATED_AT]->(s);

//create integers out of the stoptimes (to allow for calculations/ordering)
match (s:Stoptime)
set s.arrival_time_int=toInt(replace(s.arrival_time,":",""))/100
set s.departure_time_int=toInt(replace(s.departure_time,":",""))/100
;

//connect the stoptime sequences
match (s1:Stoptime)-[:PART_OF_TRIP]->(t:Trip),
      (s2:Stoptime)-[:PART_OF_TRIP]->(t)
  where s2.stop_sequence=s1.stop_sequence + 1
create (s1)-[:PRECEDES]->(s2);

