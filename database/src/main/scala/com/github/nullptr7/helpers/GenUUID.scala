package com.github.nullptr7
package helpers

import java.util.UUID

import cats.effect.Concurrent

sealed trait GenUUID[F[_]] {

  def make: F[UUID]
}

object GenUUID {

  def apply[F[_]: GenUUID]: GenUUID[F] = implicitly

  implicit def generateUUID[F[_]: Concurrent] = new GenUUID[F] {
    def make: F[UUID] = Concurrent[F].pure(UUID.randomUUID())
  }

}
