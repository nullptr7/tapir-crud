package com.github.nullptr7
package storage

import org.typelevel.log4cats.Logger

import cats._
import cats.implicits._

import com.github.nullptr7.exceptions.DatabaseException

abstract class Repository[F[_]: Logger] {

  def logAndRaise(implicit ae: ApplicativeError[F, Throwable]): PartialFunction[Throwable, F[Unit]] = { case t: Throwable =>
    Logger[F].error(t.getMessage()) *> ae.raiseError[Unit](DatabaseException(t))

  }

}
