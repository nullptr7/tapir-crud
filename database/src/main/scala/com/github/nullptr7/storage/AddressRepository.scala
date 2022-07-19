package com.github.nullptr7
package storage

import cats.effect._
import cats.syntax.all._

import models.Address

trait AddressRepository[F[_]] {

  def findAddressById(id: Long): F[Option[Address]]

  def findAddressByZip(pincode: String): F[Option[Address]]
}

object AddressRepository {

  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  import codecs.DatabaseCodecs._

  def apply[F[_]: Concurrent](session: Session[F]): AddressRepository[F] = new AddressRepository[F] {

    override def findAddressById(id: Long): F[Option[Address]] = {

      val f: Query[Long, Address] =
        sql"""
              SELECT id, street, city, state, zip 
              FROM ADDRESS
              WHERE ID = $int8
            """.query(dbToAddressDecoder)

      session
        .prepare(f)
        .use(
          _.stream(id, 32)
            .compile
            .toList
            .map(_.headOption)
        )
    }

    override def findAddressByZip(pincode: String): F[Option[Address]] = {

      val f: Query[String, Address] =
        sql"""
              SELECT ID, STREET, CITY, STATE, ZIP 
              FROM ADDRESS
              WHERE ZIP = $text
            """.query(dbToAddressDecoder)

//      session.execute(f).map(_.headOption)

      session
        .prepare(f)
        .use(
          _.stream(pincode, 32)
            .compile
            .toList
            .map(_.headOption)
        )
    }

  }

}
