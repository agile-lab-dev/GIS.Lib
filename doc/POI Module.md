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

