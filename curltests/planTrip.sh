#!/usr/bin/env bash
curl -H "Content-Type: application/json" -X POST --data @TripPlan1.json http://localhost:8080/customrest/plantrip
