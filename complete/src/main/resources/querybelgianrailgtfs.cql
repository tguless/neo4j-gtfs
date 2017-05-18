//Query a GFTS dataset
//TODO: add these to the Spring Data Repository API
//http://docs.spring.io/spring-data/neo4j/docs/current/reference/html/

//Find ANTWERP
match (s:Stop)
where upper(s.name) starts with "ANTW"
return s

//Find Turnhout part 1
match (s:Stop)
where s.name starts with "Turn"
return s

//Find Turnhout part 2
match (s:Stop)
where upper(s.name) starts with "TURN"
return s

//Find Antwerp and Turnhout
// match (s:Stop)
// where s.name starts with "Antw"
// or s.name starts with "Turn"
// return s;

match (ant:Stop), (tu:Stop)
where ant.name starts with "Antw"
AND tu.name starts with "Turn"
return distinct tu,ant;

// match (ant:Stop)
// where ant.name starts with "Antw"
// return ant.name as name
// union
// match (tu:Stop)
// where tu.name starts with "Turn"
// return tu.name as name;

//find the links between the top-level stops
match (t:Stop)<-[:PART_OF]-(:Stop),
(a:Stop)<-[:PART_OF]-(:Stop)
where t.name starts with "Turn"
AND a.name="Antwerpen-Centraal"
with t,a
match p = allshortestpaths((t)-[*]-(a))
return p
limit 10;

match (t:Stop),(a:Stop)
where t.name starts with "Turn"
AND a.name="Antwerpen-Centraal"
with t,a
match p = allshortestpaths((t)-[*]-(a))
return p
limit 10


//range queries with and without indexes
match (st:Stoptime)
where st.departure_time < "07:45:00"
return st.departure_time;

//find a DIRECT route with range conditions
match (tu:Stop {name: "Turnhout"})--(tu_st:Stoptime)
where tu_st.departure_time > "07:00:00"
AND tu_st.departure_time < "09:00:00"
with tu, tu_st
match (ant:Stop {name:"Antwerpen-Centraal"})--(ant_st:Stoptime)
where ant_st.arrival_time < "09:00:00"
AND ant_st.arrival_time > "07:00:00"
and ant_st.arrival_time > tu_st.departure_time
with ant,ant_st,tu, tu_st
match p = allshortestpaths((tu_st)-[*]->(ant_st))
with nodes(p) as n
unwind n as nodes
match (nodes)-[r]-()
return nodes,r

//Indirect routes

//find the route and the stops for the indirect route
match (t:Stop),(a:Stop)
where t.name = "Turnhout"
AND a.name="Arlon"
with t,a
match p = allshortestpaths((t)-[*]-(a))
where NONE (x in relationships(p) where type(x)="OPERATES")
return p
limit 10

//plan a specific indirect route
MATCH (tu:Stop {name:"Turnhout"})--(st_tu:Stoptime),
(ar:Stop {name:"Arlon"})--(st_ar:Stoptime),
p1=((st_tu)-[:PRECEDES*]->(st_midway_arr:Stoptime)),
(st_midway_arr)--(midway:Stop),
(midway)--(st_midway_dep:Stoptime),
p2=((st_midway_dep)-[:PRECEDES*]->(st_ar))
WHERE
st_tu.departure_time > "08:00:00"
AND st_tu.departure_time < "11:00:00"
AND st_midway_arr.arrival_time > st_tu.departure_time
AND st_midway_dep.departure_time > st_midway_arr.arrival_time
AND st_ar.arrival_time > st_midway_dep.departure_time
RETURN
tu,st_tu,ar,st_ar,p1,p2,midway
order by (st_ar.arrival_time_int-st_tu.departure_time_int) ASC
limit 1
