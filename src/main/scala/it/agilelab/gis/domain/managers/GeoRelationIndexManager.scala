package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.utils.GeoRelationManagerUtils.{ CountryPathSet, Path }
import it.agilelab.gis.core.utils.{ Configuration, Logger, ObjectPickler }
import it.agilelab.gis.domain.configuration.GeoRelationIndexManagerConfiguration
import it.agilelab.gis.domain.loader._
import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialList.GeometryList
import it.agilelab.gis.utils.ScalaUtils.recordDuration

import java.io.File
import java.util.concurrent.{ Callable, Executors }
import scala.reflect.ClassTag

/** [[GeoRelationIndexManager]] creates OSM indices, see [[GeoRelationIndexSet]] for a full list of indices created.
  *
  * @param conf OSM maps configurations.
  */
case class GeoRelationIndexManager(conf: Config) extends Configuration with Logger {

  val indexConfig: GeoRelationIndexManagerConfiguration = GeoRelationIndexManagerConfiguration(conf)
  val indexSet: GeoRelationIndexSet = createIndexSet(indexConfig)

  /** [[makeIndices]] creates indices, see [[GeocodeIndexSet]] for the full list of indices created.
    *
    * @param upperFolderPath OSM index input path.
    * @param outputPaths     serialized indices output path.
    */
  def makeIndices(upperFolderPath: String, outputPaths: Option[List[String]]): GeoRelationIndexSet = {

    // Check whether the input folder is a directory
    val mapsFolder = new File(upperFolderPath)
    if (!mapsFolder.exists()) {
      throw new IllegalArgumentException(s"$upperFolderPath doesn't exist")
    }
    if (!mapsFolder.isDirectory) {
      throw new IllegalArgumentException(s"$upperFolderPath is not a directory")
    }

    val multiCountriesPathSet: List[CountryPathSet] =
      mapsFolder
        .listFiles()
        .map(GeoRelationPathManager.getCountryPathSet)
        .toList

    val seaPathSet = GeoRelationPathManager.getSeaPathSet(indexConfig.seaInputPath)

    // If an output path is defined, we create indices and serialize them in the specified output directory.
    val maybeIndex: Option[GeoRelationIndexSet] = if (outputPaths.isDefined) {
      outputPaths.get match {
        case rPath :: sPath :: Nil =>
          makeIndex(rPath, createRailwaysGeometryList(multiCountriesPathSet))
          makeIndex(sPath, createSeaGeometryList(seaPathSet))

          Some(
            createIndexSet(
              indexConfig.copy(
                isSerializedInputPaths = true,
                inputPaths = outputPaths.get
              )))

        case Nil =>
          None

        case _ =>
          logger.warn(s"Index serialization skipped due to unexpected number of output paths $outputPaths")
          None
      }
    } else {
      None
    }

    if (maybeIndex.isDefined) {
      maybeIndex.get
    } else {
      logger.info("Creating in memory indices ...")
      val indexSet =
        recordDuration(
          createIndexSet(multiCountriesPathSet, seaPathSet),
          d => logger.info(s"Created in memory indices in $d ms"))
      indexSet
    }
  }

  /** Loads and creates an index set from the given input directories.
    *
    * @param multiCountriesPathSet input paths
    * @return index set
    */
  private def createIndexSet(
      multiCountriesPathSet: List[CountryPathSet],
      seaPathSet: Array[Path]
  ): GeoRelationIndexSet = {
    val railwaysGeometryList: Option[GeometryList[OSMRailTrack]] = multiCountriesPathSet
      .find(_.railways.nonEmpty)
      .map(_ => createRailwaysGeometryList(multiCountriesPathSet))

    val seaGeometryList: Option[GeometryList[OSMSea]] = seaPathSet
      .find(_.nonEmpty)
      .map(_ => createSeaGeometryList(seaPathSet))

    createIndexSet(railwaysGeometryList, seaGeometryList)
  }

  /** Creates an index set using the given geometry lists.
    *
    * @param railwaysGeometryList     optional geometry list for railways
    * @param seaGeometryList          optional geometry list for sea
    * @return index set
    */
  private def createIndexSet(
      railwaysGeometryList: Option[GeometryList[OSMRailTrack]],
      seaGeometryList: Option[GeometryList[OSMSea]]
  ): GeoRelationIndexSet = GeoRelationIndexSet(railwaysGeometryList, seaGeometryList)

  private def createRailwaysGeometryList(multiCountriesPathSet: List[CountryPathSet]) = {
    logger.info("Loading OSM railways file into GeometryList...")
    val railways: List[Path] = multiCountriesPathSet.flatMap(_.railways)
    val railwaysGeometryList: GeometryList[OSMRailTrack] = createRailwaysIndex(railways)
    logger.info("Done loading OSM railways file into GeometryList!")
    railwaysGeometryList
  }

  private def createSeaGeometryList(sea: Array[Path]) = {
    logger.info("Loading OSM sea file into GeometryList...")
    val seaGeometryList: GeometryList[OSMSea] = createSeaIndex(sea)
    logger.info("Done loading OSM sea file into GeometryList!")
    seaGeometryList
  }

  /** Creates the railways index
    */
  def createRailwaysIndex(railways: Seq[Path]): GeometryList[OSMRailTrack] = {
    logger.info(s"Loading OSM railways ...")
    recordDuration(
      new OSMRailwayLoader().loadIndex(railways: _*),
      d => logger.info(s"Done loading OSM railways in $d ms!")
    )
  }

  /** Creates the sea index
    */
  def createSeaIndex(sea: Array[Path]): GeometryList[OSMSea] = {
    logger.info(s"Loading OSM sea ...")
    recordDuration(
      new OSMSeaLoader().loadIndex(sea: _*),
      d => logger.info(s"Done loading OSM sea in $d ms!")
    )
  }

  /** Creates indices and serializes them in the configured output directory if specified.
    *
    * @param config indices configuration
    * @return a set of indices we care about.
    */
  private def createIndexSet(config: GeoRelationIndexManagerConfiguration): GeoRelationIndexSet = {
    val inputPaths = config.inputPaths
    if (config.isSerializedInputPaths) {
      val threadPool = Executors.newFixedThreadPool(inputPaths.size)

      val index = inputPaths match {
        case rPath :: sPath :: Nil =>
          val railways = threadPool.submit(createIndex[OSMRailTrack](rPath))
          val sea = threadPool.submit(createIndex[OSMSea](sPath))

          createIndexSet(Option(railways.get()), Option(sea.get()))

        case _ =>
          if (inputPaths.nonEmpty)
            logger.warn(s"unexpected number of elements for input paths. Given: $inputPaths")

          createIndexSet(None, None)
      }

      threadPool.shutdown()
      index

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

  private def load[T](f: => T): Callable[T] = new Callable[T] {
    override def call(): T = f
  }

  /** @param path the path in which the index will be stored
    * @param geometryList the [[GeometryList]] saved at the given path
    * @tparam T the specific [[Geometry]] class
    */
  protected def makeIndex[T <: Geometry](path: Path, geometryList: GeometryList[T])(implicit
      ctag: ClassTag[T]
  ): Unit = {
    recordDuration(
      ObjectPickler.pickle(geometryList, path),
      d => logger.info(s"Saved index for ${ctag.runtimeClass.getSimpleName} to file $path in $d ms")
    )
    System.gc()
  }

  /** Loads the index for a particular geometry [[T]]
    *
    * @param path the path in which the index is saved
    * @tparam T the specific [[Geometry]] class
    * @return a [[Callable]] version of [[GeometryList]]
    */
  protected def createIndex[T <: Geometry](path: Path)(implicit ctag: ClassTag[T]): Callable[GeometryList[T]] =
    load(
      recordDuration(
        ObjectPickler.unpickle[GeometryList[T]](path),
        d => {
          logger.info(s"Loaded index for ${ctag.runtimeClass.getSimpleName} from file $path in $d ms")
          System.gc()
        }
      )
    )
}
