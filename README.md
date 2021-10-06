# GIS library

GIS is a library to do [reverse geocoding](#reverse-geocoding-api), [route matching](#route-matching-api), and polygon
matching by leveraging OpenStreetMap maps.

## Reverse Geocoding API

The reverse geocoding API matches a GPS point onto a physical address.

## Route Matching API

The route matching API matches GPS traces onto roads.

## Build and Testing System

GIS uses [Apache Maven](https://maven.apache.org/), please refer to the Apache Maven documentation for further details.

## Running tests

To run tests you need to download a PBF file from https://download.geofabrik.de/europe/italy-latest.osm.pbf and place it
in the `src/test/resources/graphHopperSource` directory.

Then you can run tests by executing the following command:

```shell
mvn test
```

## Merging multiple PBFs

Sometimes PBFs of a specific area you want to use aren't available, the `osmium` tool allows merging multiple PBFs into
a single PBF file.

Download country maps in osm.pbf format from [https://download.geofabrik.de/](https://download.geofabrik.de/)

Then you can install and run `osmium` to merge all of them in a file called `merge.osm.pbf` by executing the following
command:

```shell
sudo apt-get install osmium-tool
osmium cat 1.osm.pbf 2.osm.pbf 3.osm.pbf -o merge.osm.pbf
```

### Creating a serialized graph

Creating a graph is expensive and slow, so to avoid building it every time we can create a serialized graph starting
from a PBF file by running the following command:

```shell
export MAVEN_OPTS = "-Xmx6G -Xms6G"
mvn exec:java -Dexec.mainClass=it.agilelab.bigdata.gis.core.apps.ConverterFromOSMToGraphHopperMap -Dexec.args="--graphLocation graphHopperMap/ --osmLocation merge.osm.pbf"
```

The serialized graph will be saved in the `graphHopperMap` directory.

_Note: you need to adjust `Xmx` and `Xms` to your specific situation._

## Map Merger

MapMerger from `src/main/scala/it/agilelab/gis/core/apps/MapMerger.scala` used to join map tables with respect some geometry condition
(e.g. join tables where geometries from a map are covered by geometries from a second map).

Update `src/main/resources/reference.conf` with the correct paths to the shapefiles, desired output path and a map with cities and related postal code.
Map with postal codes should contain all the postalcodes that are not automatically copied from the postalcodes map layer.

Output files can be used to substitute all AL8-italy files

Note: Some cities are without postalcode info. These can be updated manually:
- Biella                | 13900
- Villa San Secondo     | 14020
- Castel Madama         | 00024
- Palazzolo Acreide     | 96010
- Custonaci             | 91015
- Pieve Tesino          | 38050
- Santa Lucia di Serino | 83020
- San Cassiano          | 73020
- Ripabottoni           | 86040
- Carinaro              | 81032
- Tortorella            | 84030
- Candidoni             | 89020
- Capoliveri            | 57031
- Goro                  | 44020