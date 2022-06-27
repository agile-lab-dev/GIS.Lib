package it.agilelab.gis.domain.managers

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.utils.GeocodeManagerUtils.Path
import it.agilelab.gis.core.utils.{ Logger, ObjectPickler }
import it.agilelab.gis.domain.spatialList.GeometryList
import it.agilelab.gis.utils.ScalaUtils.{ load, recordDuration }

import java.util.concurrent.Callable
import scala.reflect.ClassTag

trait IndexManager extends Logger {

  protected def serializeIndex[T <: Geometry](path: Path, geometryList: GeometryList[T])(implicit
      ctag: ClassTag[T]
  ): Unit = {
    recordDuration(
      ObjectPickler.pickle(geometryList, path),
      d => logger.info(s"Saved index for ${ctag.runtimeClass.getSimpleName} to file $path in $d ms")
    )
    System.gc()
  }

  protected def deserializeIndex[T <: Geometry](path: Path)(implicit ctag: ClassTag[T]): Callable[GeometryList[T]] =
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
