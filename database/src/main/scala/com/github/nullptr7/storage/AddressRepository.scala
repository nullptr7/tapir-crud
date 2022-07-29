package com.github.nullptr7
package storage

import cats.effect._
import cats.syntax.all._

import models.{Address, CreateAddress}
import java.util.UUID

trait AddressRepository[F[_]] {

  def findAddressById(id: UUID): F[Option[Address]]

  def findAddressByZip(pincode: String): F[Option[Address]]

  def addAddress(address: CreateAddress): F[UUID]
}

object AddressRepository {

  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  import codecs.DatabaseCodecs._

  def apply[F[_]: Concurrent](session: Session[F]): AddressRepository[F] = new AddressRepository[F] {

    override def findAddressById(id: UUID): F[Option[Address]] = {

      val f: Query[UUID, Address] =
        sql"""
              SELECT id, street, city, state, zip 
              FROM ADDRESS
              WHERE ID = $uuid
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

    override def addAddress(address: CreateAddress): F[UUID] = {

      val insertAddressQuery: Command[UUID ~ CreateAddress] =
        sql"""
          INSERT INTO ADDRESS (ID, STREET, CITY, STATE, ZIP)
          VALUES ($uuid, $text, $text, $text, $text)          
        """.command.contramap {
          case id ~ i => id ~ i.street ~ i.city ~ i.state ~ i.zip
        }

      // TODO: Shoud generate UUID FP way...
      val uuid1 = UUID.randomUUID

      session
        .prepare(insertAddressQuery)
        //.evalTap(_ => (println(uuid1)).pure[F])
        .use(_.execute(uuid1 ~ address))
        .as(uuid1)
    }

  }

}
