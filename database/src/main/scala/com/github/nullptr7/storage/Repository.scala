package com.github.nullptr7
package storage

import org.typelevel.log4cats.Logger

import cats._
import cats.implicits._

import exceptions.DatabaseException

abstract class Repository[F[_]: Logger] {

  final protected[storage] def logAndRaise(implicit ae: ApplicativeError[F, Throwable]): PartialFunction[Throwable, F[Unit]] = { case t: Throwable =>
    Logger[F].error(t.getMessage()) *> ae.raiseError[Unit](DatabaseException(t))

  }

}
