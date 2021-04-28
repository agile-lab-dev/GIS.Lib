package it.agilelab.bigdata.gis.core.utils

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.file.Paths

object ObjectPickler {

  def pickle[T](item: T, path: String): Unit = {

    val root = Paths.get(path).getParent.toFile
    if (!root.exists()) {
      root.mkdirs()
    }

    val oos = new ObjectOutputStream(new FileOutputStream(path))
    try {
      oos.writeObject(item)
    } finally {
      oos.close()
    }
  }

  def unpickle[T](path: String): T = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    try {
      ois.readObject.asInstanceOf[T]
    } finally {
      ois.close()
    }
  }

}
