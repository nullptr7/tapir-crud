package com.github.nullptr7
package storage

import org.typelevel.log4cats.Logger

import cats.effect._
import cats.syntax.all._

import models.{Address, AddressId, CreateAddress}

trait AddressRepository[F[_]] extends Repository[F] {

  def findAddressById(id: AddressId): F[Option[Address]]

  def findAddressByZip(pincode: String): F[Option[Address]]

  def addAddress(address: CreateAddress): F[AddressId]
}

object AddressRepository {

  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  import codecs.DatabaseCodecs._
  import optics.ID
  import models.implicits.AddressIdIso

  def apply[F[_]: Async: Logger](session: Session[F]): AddressRepository[F] =
    new AddressRepository[F] {

      override def findAddressById(id: AddressId): F[Option[Address]] = {

        val f: Query[AddressId, Address] =
          sql"""
              SELECT id, street, city, state, zip 
              FROM ADDRESS
              WHERE ID = ${addressIdCodec.asEncoder}
            """.query(dbToAddressDecoder)

        session
          .prepare(f)
          .use(
            _.stream(id, 32)
              .compile
              .toList
              .map(_.headOption)
          )
          .onError(logAndRaise)
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
          .onError(logAndRaise)
      }

      override def addAddress(address: CreateAddress): F[AddressId] = {

        val insertAddressQuery: Command[AddressId ~ CreateAddress] =
          sql"""
          INSERT INTO ADDRESS (ID, STREET, CITY, STATE, ZIP)
          VALUES (${addressIdCodec.asEncoder}, $text, $text, $text, $text)          
        """.command.contramap { case id ~ i =>
            id ~ i.street ~ i.city ~ i.state ~ i.zip
          }

        session
          .prepare(insertAddressQuery)
          .use { cmd =>
            ID.make[F, AddressId]
              .flatMap { id =>
                cmd.execute(id ~ address).as(id)
              }
              .onError(logAndRaise)
          }
      }

    }

}
