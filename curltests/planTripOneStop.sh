#!/usr/bin/env bash
curl -H "Content-Type: application/json" -X POST --data @TripPlanOneStop.json http://localhost:8080/customrest/planTripOneStop | python -m json.tool
