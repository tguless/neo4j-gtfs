#!/usr/bin/env bash
curl -H "Content-Type: application/json" -X POST --data @TripPlanOneTransfer.json http://localhost:8080/customrest/planTripOneTransfer | python -m json.tool
