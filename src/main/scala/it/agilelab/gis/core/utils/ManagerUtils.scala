package it.agilelab.gis.core.utils

import java.io.File

/** @author Agile Lab s.r.l.
  */
trait ManagerUtils {
  type Path = String

  def filterPaths(folder: File, regex: String, extension: String = "shp"): Array[Path] =
    folder.listFiles().map(_.getAbsolutePath).filter(_.matches(s".*$regex.*.$extension"))
}
