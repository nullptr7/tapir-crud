package com.github.nullptr7
package helpers

import scala.reflect.ClassTag

import cats.effect._

import pureconfig.module.catseffect.syntax._
import pureconfig.{ConfigReader, ConfigSource}

import configurations.types._

sealed trait ConfigLoader[F[_]] {

  final protected lazy val source = ConfigSource.default

  def load[Conf: ConfigReader: ClassTag, CT <: ConfigType](implicit ct: CT): Resource[F, Conf]
}

object ConfigLoader {

  def apply[F[_]: ConfigLoader]: ConfigLoader[F] = implicitly

  implicit def forSync[F[_]: Sync]: ConfigLoader[F] =
    new ConfigLoader[F] {

      def load[Conf: ConfigReader: ClassTag, CT <: ConfigType](implicit ct: CT): Resource[F, Conf] =
        Resource
          .eval(source.at(ct.namespace.value).loadF[F, Conf]())

    }

}
