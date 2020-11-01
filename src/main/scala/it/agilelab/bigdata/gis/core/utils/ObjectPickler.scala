package it.agilelab.bigdata.gis.core.utils

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

object ObjectPickler {

  def pickle[T](item: T, path: String): Unit = {

    val oos = new ObjectOutputStream(new FileOutputStream(path))
    oos.writeObject(item)
    oos.close()

  }

  def unpickle[T](path: String): T = {
    val ois = new ObjectInputStream(new FileInputStream(path))
    val item = ois.readObject.asInstanceOf[T]
    ois.close()

    item
  }

}
