package com.github.nullptr7
package helpers

import scala.reflect.ClassTag

import cats.effect._

import pureconfig.module.catseffect.syntax._
import pureconfig.{ConfigReader, ConfigSource}

import configurations.types._

trait ConfigLoader[F[_]] {

  def load[Conf: ConfigReader: ClassTag](configType: ConfigType): Resource[F, Conf]
}

object ConfigLoader {

  def apply[F[_]: ConfigLoader]: ConfigLoader[F] = implicitly

  implicit def forSync[F[_]: Sync]: ConfigLoader[F] =
    new ConfigLoader[F] {

      def load[Conf: ConfigReader: ClassTag](configType: ConfigType): Resource[F, Conf] = Resource.eval {
        configType.location match {
          case None       => ConfigSource.default.at(configType.namespace.value).loadF[F, Conf]()
          case Some(path) => ConfigSource.file(path.value).at(configType.namespace.value).loadF[F, Conf]()
        }
      }

    }

}
