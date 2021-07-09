#!/usr/bin/env bash

set -e

mvn exec:java -Dexec.mainClass=it.agilelab.gis.core.apps.ConverterFromOSMToGraphHopperMap -Dexec.args="--graphLocation src/test/resources/graphHopper/ --osmLocation src/test/resources/graphHopperSource/milan.osm.pbf"