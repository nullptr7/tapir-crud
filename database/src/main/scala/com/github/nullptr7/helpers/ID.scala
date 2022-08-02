package com.github.nullptr7
package optics

import cats.Functor
import cats.implicits._

import helpers.GenUUID

object ID {

  def make[F[_]: Functor: GenUUID, A: IsUUID]: F[A] =
    GenUUID[F]
      .make
      .map(IsUUID[A]._uuid.get)

}
