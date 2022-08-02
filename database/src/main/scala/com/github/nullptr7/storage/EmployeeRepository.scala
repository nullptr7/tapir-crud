package com.github.nullptr7
package storage

import cats.effect._
import cats.implicits._

import models.{Address, Employee}

trait EmployeeRepository[F[_]] {

  def findAllEmployees: F[List[Employee]]

  def findById(id: Long): F[Option[Employee]]

}

object EmployeeRepository {

  import skunk._
  import skunk.implicits._

  import codecs.DatabaseCodecs.dbToEmployeeDecoder

  def apply[F[_]: Concurrent](session: Session[F]): EmployeeRepository[F] =
    new EmployeeRepository[F] {

      override def findAllEmployees: F[List[Employee]] = {

        val empQuery: Query[Void, Int ~ String ~ Int ~ Double ~ Address] =
          sql"""
              SELECT e.id, e.name, e.age, e.salary, a.id, a.street, a.city, a.state, a.zip 
              FROM EMPLOYEE e, ADDRESS a
              WHERE e.address = a.id
            """.query(dbToEmployeeDecoder)

        session
          .execute(empQuery)
          .map(_.map {
            case id ~ name ~ age ~ salary ~ address =>
              Employee(id, name, age, salary, address)
          })
          .attemptTap {
            case Left(error)  => Concurrent[F].raiseError[List[Employee]](error)
            case Right(value) => value.pure[F]
          }
      }

      override def findById(id: Long): F[Option[Employee]] = {
        import skunk.codec.all._

        val empQueryById: Query[Long, Int ~ String ~ Int ~ Double ~ Address] =
          sql"""
             SELECT e.id, e.name, e.age, e.salary, a.id, a.street, a.city, a.state, a.zip
             FROM EMPLOYEE e, ADDRESS a
             WHERE e.id = $int8 AND e.address = a.id
           """.query(dbToEmployeeDecoder)

        session
          .prepare(empQueryById)
          .use(_.option(id))
          .map(_.map {
            case id ~ name ~ age ~ salary ~ address =>
              Employee(id, name, age, salary, address)
          })
      }

    }

}
