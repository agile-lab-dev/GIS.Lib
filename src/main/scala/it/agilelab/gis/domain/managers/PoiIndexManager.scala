package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.Configuration
import it.agilelab.gis.core.utils.GeocodeManagerUtils.Path
import it.agilelab.gis.domain.configuration.PoiIndexManagerConfiguration
import it.agilelab.gis.domain.loader._
import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialList.GeometryList
import it.agilelab.gis.utils.ScalaUtils.recordDuration

import java.io.File
import java.util.concurrent.Executors

/** [[PoiIndexManager]] creates OSM POI indices, see [[PoiIndexSet]] for a full list of indices created.
  *
  * @param conf OSM POI configurations.
  */
case class PoiIndexManager(conf: Config) extends Configuration with IndexManager {

  val indexConfig: PoiIndexManagerConfiguration = PoiIndexManagerConfiguration(conf)
  val poiPathManager: PoiPathManager = PoiPathManager(indexConfig.pathConf)
  val indexSet: PoiIndexSet = createIndexSet(indexConfig)

  private def createIndexSet(config: PoiIndexManagerConfiguration): PoiIndexSet = {
    val inputPaths = config.inputPaths
    if (config.isSerializedInputPaths) {
      inputPaths match {
        case amenityPath :: landusePath :: leisurePath :: naturalPath :: shopPath :: Nil =>
          logger.info(s"Loading POI index from files $amenityPath $landusePath $leisurePath $naturalPath $shopPath")
          val threadPool = Executors.newFixedThreadPool(inputPaths.size)
          val amenityIdx = threadPool.submit(deserializeIndex[OSMPoiAmenity](amenityPath))
          val landuseIdx = threadPool.submit(deserializeIndex[OSMPoiLanduse](landusePath))
          val leisureIdx = threadPool.submit(deserializeIndex[OSMPoiLeisure](leisurePath))
          val naturalIdx = threadPool.submit(deserializeIndex[OSMPoiNatural](naturalPath))
          val shopIdx = threadPool.submit(deserializeIndex[OSMPoiShop](shopPath))
          val index =
            createIndexSet(amenityIdx.get(), landuseIdx.get(), leisureIdx.get(), naturalIdx.get(), shopIdx.get())
          threadPool.shutdown()
          index
        case _ =>
          throw new IllegalArgumentException(
            s"the list of input paths should be five " +
              s"different paths (amenity, landuse, leisure, natural and shop). The list of input path was: $inputPaths")
      }
    } else {
      inputPaths match {
        case path :: Nil => makeIndices(path, config.outputPaths)
        case _ =>
          throw new IllegalArgumentException(
            s"the list of input paths should be a single path. " +
              s"The list of input path was: $inputPaths")
      }
    }
  }

  private def makeIndices(upperFolderPath: String, outputPaths: Option[List[String]]): PoiIndexSet = {

    // Check whether the input folder is a directory
    val mapsFolder = new File(upperFolderPath)
    if (!mapsFolder.exists()) {
      throw new IllegalArgumentException(s"$upperFolderPath doesn't exist")
    }
    if (!mapsFolder.isDirectory) {
      throw new IllegalArgumentException(s"$upperFolderPath is not a directory")
    }

    val multiCountriesPathSet: List[PoiPathSet] =
      mapsFolder
        .listFiles()
        .map(poiPathManager.getCountryPathSet)
        .toList

    // If an output path is defined, we create indices and serialize them in the specified output directory.
    val maybeIndex: Option[PoiIndexSet] = if (outputPaths.isDefined) {
      outputPaths.get match {
        case amenityPath :: landusePath :: leisurePath :: naturalPath :: shopPath :: Nil =>
          logger.info(s"Saving POI OSM index in $amenityPath $landusePath $leisurePath $naturalPath $shopPath")
          serializeIndex[OSMPoiAmenity](amenityPath, createAmenityGeometryList(multiCountriesPathSet))
          serializeIndex[OSMPoiLanduse](landusePath, createLanduseGeometryList(multiCountriesPathSet))
          serializeIndex[OSMPoiLeisure](leisurePath, createLeisureGeometryList(multiCountriesPathSet))
          serializeIndex[OSMPoiNatural](naturalPath, createNaturalGeometryList(multiCountriesPathSet))
          serializeIndex[OSMPoiShop](shopPath, createShopGeometryList(multiCountriesPathSet))

          Some(
            createIndexSet(
              indexConfig.copy(
                isSerializedInputPaths = true,
                inputPaths = outputPaths.get
              )))
        case _ =>
          logger.warn(s"POI Index serialization skipped due to unexpected output paths $outputPaths")
          None
      }
    } else {
      None
    }

    if (maybeIndex.isDefined) {
      maybeIndex.get
    } else {
      logger.info("Creating in memory POI indices ...")
      val indexSet =
        recordDuration(
          createIndexSet(multiCountriesPathSet),
          d => logger.info(s"Created in memory POI indices in $d ms"))
      indexSet
    }
  }

  private def createIndexSet(multiCountriesPathSet: List[PoiPathSet]): PoiIndexSet = {
    val amenityGeometryList: GeometryList[OSMPoiAmenity] = createAmenityGeometryList(multiCountriesPathSet)
    val landuseGeometryList: GeometryList[OSMPoiLanduse] = createLanduseGeometryList(multiCountriesPathSet)
    val leisureGeometryList: GeometryList[OSMPoiLeisure] = createLeisureGeometryList(multiCountriesPathSet)
    val naturalGeometryList: GeometryList[OSMPoiNatural] = createNaturalGeometryList(multiCountriesPathSet)
    val shopGeometryList: GeometryList[OSMPoiShop] = createShopGeometryList(multiCountriesPathSet)
    createIndexSet(amenityGeometryList, landuseGeometryList, leisureGeometryList, naturalGeometryList, shopGeometryList)
  }

  private def createIndexSet(
      amenityGeometryList: GeometryList[OSMPoiAmenity],
      landuseGeometryList: GeometryList[OSMPoiLanduse],
      leisureGeometryList: GeometryList[OSMPoiLeisure],
      naturalGeometryList: GeometryList[OSMPoiNatural],
      shopGeometryList: GeometryList[OSMPoiShop]
  ): PoiIndexSet =
    PoiIndexSet(amenityGeometryList, landuseGeometryList, leisureGeometryList, naturalGeometryList, shopGeometryList)

  private def createAmenityGeometryList(multiCountriesPathSet: List[PoiPathSet]) = {
    logger.info("Loading OSM POI amenity file into GeometryList...")
    val amenityPathList: List[Path] = multiCountriesPathSet.flatMap(_.amenityPath)
    val amenityGeometryList: GeometryList[OSMPoiAmenity] = createAmenityIndex(amenityPathList)
    logger.info("Done loading OSM POI amenity file into GeometryList!")
    amenityGeometryList
  }

  private def createLanduseGeometryList(multiCountriesPathSet: List[PoiPathSet]) = {
    logger.info("Loading OSM POI landuse file into GeometryList...")
    val landusePathList: List[Path] = multiCountriesPathSet.flatMap(_.landusePath)
    val landuseGeometryList: GeometryList[OSMPoiLanduse] = createLanduseIndex(landusePathList)
    logger.info("Done loading OSM POI landuse file into GeometryList!")
    landuseGeometryList
  }

  private def createLeisureGeometryList(multiCountriesPathSet: List[PoiPathSet]) = {
    logger.info("Loading OSM POI leisure file into GeometryList...")
    val leisurePathList: List[Path] = multiCountriesPathSet.flatMap(_.leisurePath)
    val leisureGeometryList: GeometryList[OSMPoiLeisure] = createLeisureIndex(leisurePathList)
    logger.info("Done loading OSM POI leisure file into GeometryList!")
    leisureGeometryList
  }

  private def createNaturalGeometryList(multiCountriesPathSet: List[PoiPathSet]) = {
    logger.info("Loading OSM POI natural file into GeometryList...")
    val naturalPathList: List[Path] = multiCountriesPathSet.flatMap(_.naturalPath)
    val naturalGeometryList: GeometryList[OSMPoiNatural] = createNaturalIndex(naturalPathList)
    logger.info("Done loading OSM POI natural file into GeometryList!")
    naturalGeometryList
  }

  private def createShopGeometryList(multiCountriesPathSet: List[PoiPathSet]) = {
    logger.info("Loading OSM POI shop file into GeometryList...")
    val shopPathList: List[Path] = multiCountriesPathSet.flatMap(_.shopPath)
    val shopGeometryList: GeometryList[OSMPoiShop] = createShopIndex(shopPathList)
    logger.info("Done loading OSM POI shop file into GeometryList!")
    shopGeometryList
  }

  private def createAmenityIndex(amenityPathList: List[Path]): GeometryList[OSMPoiAmenity] = {
    logger.info(s"Loading OSM POI amenity ...")
    recordDuration(
      new OSMPoiAmenityLoader().loadIndex(amenityPathList: _*),
      d => logger.info(s"Done loading OSM POI amenity in $d ms!")
    )
  }

  private def createLanduseIndex(landusePathList: List[Path]): GeometryList[OSMPoiLanduse] = {
    logger.info(s"Loading OSM POI landuse ...")
    recordDuration(
      new OSMPoiLanduseLoader().loadIndex(landusePathList: _*),
      d => logger.info(s"Done loading OSM POI landuse in $d ms!")
    )
  }

  private def createLeisureIndex(leisurePathList: List[Path]): GeometryList[OSMPoiLeisure] = {
    logger.info(s"Loading OSM POI leisure ...")
    recordDuration(
      new OSMPoiLeisureLoader().loadIndex(leisurePathList: _*),
      d => logger.info(s"Done loading OSM POI leisure in $d ms!")
    )
  }

  private def createNaturalIndex(naturalPathList: List[Path]): GeometryList[OSMPoiNatural] = {
    logger.info(s"Loading OSM POI natural ...")
    recordDuration(
      new OSMPoiNaturalLoader().loadIndex(naturalPathList: _*),
      d => logger.info(s"Done loading OSM POI natural in $d ms!")
    )
  }

  private def createShopIndex(shopPathList: List[Path]): GeometryList[OSMPoiShop] = {
    logger.info(s"Loading OSM POI shop ...")
    recordDuration(
      new OSMPoiShopLoader().loadIndex(shopPathList: _*),
      d => logger.info(s"Done loading OSM POI shop in $d ms!")
    )
  }

}
