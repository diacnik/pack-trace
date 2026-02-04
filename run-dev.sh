#!/bin/bash

# Load environment variables from .env file
export $(cat .env | xargs)

# Run Quarkus in dev mode
./gradlew quarkusDev
