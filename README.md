# GIS.Lib

GIS is a library to calculate ReverseGeocoding, MapMatching and the belonging of a point to an area of interest. It is
possible use OSM maps or HERE maps or extends Loader for other maps.

### Test GraphHopper

Download country maps in osm.pbf format from https://download.geofabrik.de/europe.html
[OPTIONAL] Merge all downloaded files with the following command: (only if you download multiple countries)

```
sudo apt-get install osmium-tool
osmium cat 1.osm.pbf 2.osm.pbf 3.osm.pbf -o merge.osm.pbf
```

Create graph:

```
export MAVEN_OPTS = "-Xmx6G -Xms6G"
mvn exec:java -Dexec.mainClass=it.agilelab.bigdata.gis.core.apps.ConverterFromOSMToGraphHopperMap -Dexec.args="--graphLocation graphHopperMap/ --osmLocation merge.osm.pbf"
```