#!/usr/bin/env bash
curl -H "Content-Type: application/json" -X POST --data @TripPlanNoTransfer.json http://localhost:8080/customrest/planTripNoTransfer | python -m json.tool
