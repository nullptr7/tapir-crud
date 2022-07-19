package com.github.nullptr7
package config

import scala.reflect.ClassTag

import cats.effect._

import pureconfig.module.catseffect.syntax._
import pureconfig.{ConfigReader, ConfigSource}

trait ConfigLoader[F[_], Conf] {

  val load: Resource[F, Conf]
}

object ConfigLoader {

  def init[F[_]: Sync, Conf: ConfigReader: ClassTag](
    configLocation: Option[String],
    namespace:      String
  ) = new ConfigLoader[F, Conf] {

    val load: Resource[F, Conf] =
      Resource.eval {
        configLocation match {
          case None       => ConfigSource.default.at(namespace).loadF[F, Conf]()
          case Some(path) => ConfigSource.file(path).at(namespace).loadF[F, Conf]()
        }
      }

  }

}
