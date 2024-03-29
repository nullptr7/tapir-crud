package com.github.nullptr7
package optics

import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.kernel.Sync
import cats.implicits._

import helpers.GenUUID

final object ID {

  /** @note Probably an overkill to log this instead of just returning `GenUUID[F].make.map(IsUUID[A]._uuid.get)`
    */
  def make[F[_]: Sync: GenUUID, A: IsUUID]: F[A] =
    for {
      logger <- Slf4jLogger.create[F]
      id     <- GenUUID[F].make.map(IsUUID[A]._uuid.get)
      _      <- logger.info(s"Generation unique identification ${id.toString}")
    } yield id

}
