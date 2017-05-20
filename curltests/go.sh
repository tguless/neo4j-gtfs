#!/usr/bin/env bash
curl -H "Content-Type: application/json" -X POST --data @payload1.json http://localhost:8080/customrest/test2?sort=departureTimeInt
