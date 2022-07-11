# PoI Module

This module makes it possible to search, given a geographic coordinate, for the presence of the following types of points of interest:
- amenity
- landuse
- leisure
- natural
- shop

The entry point of the module is the class `it.agilelab.gis.domain.managers.PoiManager`.

At startup, the module creates (or loads) an index for each different type of point of interest. Different geometries belonging to the same type of point of interest converge in the same index.

## File structure for index creation

The module supports the presence of multiple maps each containing the required structure for the various PoIs.

The module supports the following geometries for the index creation:
- lines
- multipolygons
- points

To specify the type of geometry that a file represents, the type itself must be present in the file name. For example, a file containing geometries of type `points` must have in the name the word `points` like in `points.shp`.

If you want to generate the indexes for the `italy-centro` and `italy-sud` maps, the following structure is expected:

<pre>
.
├── italy-centro
│   ├── amenity
│   │   ├── lines.dbf
│   │   ├── lines.prj
│   │   ├── lines.shp
│   │   ├── lines.shx
│   │   ├── multipolygons.dbf
│   │   ├── multipolygons.prj
│   │   ├── multipolygons.shp
│   │   ├── multipolygons.shx
│   │   ├── points.dbf
│   │   ├── points.prj
│   │   ├── points.shp
│   │   └── points.shx
│   ├── landuse
│   │   ├── lines.dbf
│   │   ├── lines.prj
│   │   ├── lines.shp
│   │   ├── lines.shx
│   │   ├── multipolygons.dbf
│   │   ├── multipolygons.prj
│   │   ├── multipolygons.shp
│   │   ├── multipolygons.shx
│   │   ├── points.dbf
│   │   ├── points.prj
│   │   ├── points.shp
│   │   └── points.shx
│   ├── leisure
│   │   ├── lines.dbf
│   │   ├── lines.prj
│   │   ├── lines.shp
│   │   ├── lines.shx
│   │   ├── multipolygons.dbf
│   │   ├── multipolygons.prj
│   │   ├── multipolygons.shp
│   │   ├── multipolygons.shx
│   │   ├── points.dbf
│   │   ├── points.prj
│   │   ├── points.shp
│   │   └── points.shx
│   ├── natural
│   │   ├── lines.dbf
│   │   ├── lines.prj
│   │   ├── lines.shp
│   │   ├── lines.shx
│   │   ├── multipolygons.dbf
│   │   ├── multipolygons.prj
│   │   ├── multipolygons.shp
│   │   ├── multipolygons.shx
│   │   ├── points.dbf
│   │   ├── points.prj
│   │   ├── points.shp
│   │   └── points.shx
│   └── shop
│       ├── lines.dbf
│       ├── lines.prj
│       ├── lines.shp
│       ├── lines.shx
│       ├── multipolygons.dbf
│       ├── multipolygons.prj
│       ├── multipolygons.shp
│       ├── multipolygons.shx
│       ├── points.dbf
│       ├── points.prj
│       ├── points.shp
│       └── points.shx
└── italy-sud
    ├── amenity
    │   ├── lines.dbf
    │   ├── lines.prj
    │   ├── lines.shp
    │   ├── lines.shx
    │   ├── multipolygons.dbf
    │   ├── multipolygons.prj
    │   ├── multipolygons.shp
    │   ├── multipolygons.shx
    │   ├── points.dbf
    │   ├── points.prj
    │   ├── points.shp
    │   └── points.shx
    ├── landuse
    │   ├── lines.dbf
    │   ├── lines.prj
    │   ├── lines.shp
    │   ├── lines.shx
    │   ├── multipolygons.dbf
    │   ├── multipolygons.prj
    │   ├── multipolygons.shp
    │   ├── multipolygons.shx
    │   ├── points.dbf
    │   ├── points.prj
    │   ├── points.shp
    │   └── points.shx
    ├── leisure
    │   ├── lines.dbf
    │   ├── lines.prj
    │   ├── lines.shp
    │   ├── lines.shx
    │   ├── multipolygons.dbf
    │   ├── multipolygons.prj
    │   ├── multipolygons.shp
    │   ├── multipolygons.shx
    │   ├── points.dbf
    │   ├── points.prj
    │   ├── points.shp
    │   └── points.shx
    ├── natural
    │   ├── lines.dbf
    │   ├── lines.prj
    │   ├── lines.shp
    │   ├── lines.shx
    │   ├── multipolygons.dbf
    │   ├── multipolygons.prj
    │   ├── multipolygons.shp
    │   ├── multipolygons.shx
    │   ├── points.dbf
    │   ├── points.prj
    │   ├── points.shp
    │   └── points.shx
    └── shop
        ├── lines.dbf
        ├── lines.prj
        ├── lines.shp
        ├── lines.shx
        ├── multipolygons.dbf
        ├── multipolygons.prj
        ├── multipolygons.shp
        ├── multipolygons.shx
        ├── points.dbf
        ├── points.prj
        ├── points.shp
        └── points.shx
</pre>

### Notes on shape files

In order to extract point-of-interest information from appropriate columns, shape files must be produced with the `ogr2ogr` tool using the following configuration file `osmconf.ini`:
```
#
# Configuration file for OSM import
#

# put here the name of keys, or key=value, for ways that are assumed to be polygons if they are closed
# see http://wiki.openstreetmap.org/wiki/Map_Features
closed_ways_are_polygons=aeroway,amenity,boundary,building,craft,geological,historic,landuse,leisure,military,natural,office,place,shop,sport,tourism,highway=platform,public_transport=platform

# Uncomment to avoid laundering of keys ( ':' turned into '_' )
#attribute_name_laundering=no

# Some tags, set on ways and when building multipolygons, multilinestrings or other_relations,
# are normally filtered out early, independent of the 'ignore' configuration below.
# Uncomment to disable early filtering. The 'ignore' lines below remain active.
#report_all_tags=yes

# uncomment to report all nodes, including the ones without any (significant) tag
#report_all_nodes=yes

# uncomment to report all ways, including the ones without any (significant) tag
#report_all_ways=yes

[points]
# common attributes
osm_id=yes
osm_version=no
osm_timestamp=no
osm_uid=no
osm_user=no
osm_changeset=no

# keys to report as OGR fields
attributes=name,amenity,landuse,leisure,natural,shop
# keys that, alone, are not significant enough to report a node as a OGR point
unsignificant=created_by,converted_by,source,time,ele,attribution
# keys that should NOT be reported in the "other_tags" field
ignore=created_by,converted_by,source,time,ele,note,todo,openGeoDB:,fixme,FIXME
# uncomment to avoid creation of "other_tags" field
other_tags=no
# uncomment to create "all_tags" field. "all_tags" and "other_tags" are exclusive
#all_tags=yes

[lines]
# common attributes
osm_id=yes
osm_version=no
osm_timestamp=no
osm_uid=no
osm_user=no
osm_changeset=no

# keys to report as OGR fields
attributes=name,amenity,landuse,leisure,natural,shop
# type of attribute 'foo' can be changed with something like
#foo_type=Integer/Real/String/DateTime

# keys that should NOT be reported in the "other_tags" field
ignore=created_by,converted_by,source,time,ele,note,todo,openGeoDB:,fixme,FIXME
# uncomment to avoid creation of "other_tags" field
other_tags=no
# uncomment to create "all_tags" field. "all_tags" and "other_tags" are exclusive
#all_tags=yes

#computed_attributes must appear before the keywords _type and _sql
computed_attributes=z_order
z_order_type=Integer
# Formula based on https://github.com/openstreetmap/osm2pgsql/blob/master/style.lua#L13
# [foo] is substituted by value of tag foo. When substitution is not wished, the [ character can be escaped with \[ in literals
# Note for GDAL developers: if we change the below formula, make sure to edit ogrosmlayer.cpp since it has a hardcoded optimization for this very precise formula
z_order_sql="SELECT (CASE [highway] WHEN 'minor' THEN 3 WHEN 'road' THEN 3 WHEN 'unclassified' THEN 3 WHEN 'residential' THEN 3 WHEN 'tertiary_link' THEN 4 WHEN 'tertiary' THEN 4 WHEN 'secondary_link' THEN 6 WHEN 'secondary' THEN 6 WHEN 'primary_link' THEN 7 WHEN 'primary' THEN 7 WHEN 'trunk_link' THEN 8 WHEN 'trunk' THEN 8 WHEN 'motorway_link' THEN 9 WHEN 'motorway' THEN 9 ELSE 0 END) + (CASE WHEN [bridge] IN ('yes', 'true', '1') THEN 10 ELSE 0 END) + (CASE WHEN [tunnel] IN ('yes', 'true', '1') THEN -10 ELSE 0 END) + (CASE WHEN [railway] IS NOT NULL THEN 5 ELSE 0 END) + (CASE WHEN [layer] IS NOT NULL THEN 10 * CAST([layer] AS INTEGER) ELSE 0 END)"

[multipolygons]
# common attributes
# note: for multipolygons, osm_id=yes instantiates a osm_id field for the id of relations
# and a osm_way_id field for the id of closed ways. Both fields are exclusively set.
osm_id=yes
osm_version=no
osm_timestamp=no
osm_uid=no
osm_user=no
osm_changeset=no

# keys to report as OGR fields
attributes=name,amenity,landuse,leisure,natural,shop
# keys that should NOT be reported in the "other_tags" field
ignore=area,created_by,converted_by,source,time,ele,note,todo,openGeoDB:,fixme,FIXME
# uncomment to avoid creation of "other_tags" field
other_tags=no
# uncomment to create "all_tags" field. "all_tags" and "other_tags" are exclusive
#all_tags=yes

[multilinestrings]
# common attributes
osm_id=yes
osm_version=no
osm_timestamp=no
osm_uid=no
osm_user=no
osm_changeset=no

# keys to report as OGR fields
attributes=name,type
# keys that should NOT be reported in the "other_tags" field
ignore=area,created_by,converted_by,source,time,ele,note,todo,openGeoDB:,fixme,FIXME
# uncomment to avoid creation of "other_tags" field
#other_tags=no
# uncomment to create "all_tags" field. "all_tags" and "other_tags" are exclusive
#all_tags=yes

[other_relations]
# common attributes
osm_id=yes
osm_version=no
osm_timestamp=no
osm_uid=no
osm_user=no
osm_changeset=no

# keys to report as OGR fields
attributes=name,type
# keys that should NOT be reported in the "other_tags" field
ignore=area,created_by,converted_by,source,time,ele,note,todo,openGeoDB:,fixme,FIXME
# uncomment to avoid creation of "other_tags" field
#other_tags=no
# uncomment to create "all_tags" field. "all_tags" and "other_tags" are exclusive
#all_tags=yes

```
It's possible to define an alternate configuration path exporting the environment variable `OSM_CONFIG_FILE`. For example:
```
export OSM_CONFIG_FILE=/<path>/<to>/<your>/osmconf.ini
```

## Configuration

The module needs the following configuration keys:
```
  poi {
    filter_empty_amenity = true
    filter_empty_landuse = true
    filter_empty_leisure = true
    filter_empty_natural = true
    filter_empty_shop = true
    index {
      is_serialized_input_paths = false

      input_paths = ["src/test/resources/osm/maps/poi/"]

      serialized_output_paths = [
        "src/test/resources/osm/maps-index/amenity",
        "src/test/resources/osm/maps-index/landuse",
        "src/test/resources/osm/maps-index/leisure",
        "src/test/resources/osm/maps-index/natural",
        "src/test/resources/osm/maps-index/shop"
      ]
      path {
        amenity {
          folderName = "amenity"
          regexFilterFileNames = "(lines|multipolygons|points)"
        }
        landuse {
          folderName = "landuse"
          regexFilterFileNames = "(lines|multipolygons|points)"
        }
        leisure {
          folderName = "leisure"
          regexFilterFileNames = "(lines|multipolygons|points)"
        }
        natural {
          folderName = "natural"
          regexFilterFileNames = "(lines|multipolygons|points)"
        }
        shop {
          folderName = "shop"
          regexFilterFileNames = "(lines|multipolygons|points)"
        }
      }
    }
  }
```

### filter_empty_amenity
Boolean values (true or false) are allowed. If true, results for which the amenity tag is not present or is empty are excluded.

### filter_empty_landuse
Boolean values (true or false) are allowed. If true, results for which the landuse tag is not present or is empty are excluded.

### filter_empty_leisure
Boolean values (true or false) are allowed. If true, results for which the leisure tag is not present or is empty are excluded.

### filter_empty_natural
Boolean values (true or false) are allowed. If true, results for which the natural tag is not present or is empty are excluded.

### filter_empty_shop
Boolean values (true or false) are allowed. If true, results for which the shop tag is not present or is empty are excluded.

### index.is_serialized_input_paths
Boolean values (true or false) are allowed. If present and true, tells the module that there are already serialized files available for the different indexes that can be loaded at startup.

### index.input_paths
Array of strings is expected. If `is_serialized_input_paths` is not present or is false, this key expects a single element representing the root folder from which to begin searching shape files for index construction. Otherwise, it expects an array of 5 elements specifying the path and name of the indexes already serialized, in the following **order**:
- amenity
- landuse
- leisure
- natural
- shop

For example:
```
input_paths = [
        "src/test/resources/osm/maps-index/amenity",
        "src/test/resources/osm/maps-index/landuse",
        "src/test/resources/osm/maps-index/leisure",
        "src/test/resources/osm/maps-index/natural",
        "src/test/resources/osm/maps-index/shop"
      ]
```

### index.serialized_output_paths
Array of strings is expected. If present, tells the module to serialize to file system the indexes built at startup. The ***order*** is important and must be the same as the `input_paths` key.

### index.path.[PoI type].folderName
The module expects shape files of a particular [PoI type] to be present within a folder with the name specified by this key. Shape files must have `.shp` extension.

### index.path.[PoI type].regexFilterFileNames
The module expects the name of the shape files of a particular [PoI type] to be verified by the regular expression specified by this key.

