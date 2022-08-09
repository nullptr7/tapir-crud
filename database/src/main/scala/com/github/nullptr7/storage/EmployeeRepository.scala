package com.github.nullptr7
package storage

import org.typelevel.log4cats.Logger

import cats.effect._

import models.{CreateEmployee, Employee, EmployeeId}
import optics.ID

trait EmployeeRepository[F[_]] extends Repository[F] {

  def findAllEmployees: F[List[Employee]]

  def findById(id: Long): F[Option[Employee]]

  def addEmployee(employee: CreateEmployee): F[EmployeeId]

}

object EmployeeRepository {

  import skunk._
  import skunk.implicits._
  import models.{AddressId, CreateAddress, CreateEmployee, Employee, EmployeeCode, EmployeeId}
  import codecs.DatabaseCodecs.{addressIdCodec, dbToEmployeeDecoder, employeeCodeCodec, employeeIdCodec}
  import skunk.codec.all._
  import models.implicits.{AddressIdIso, EmployeeCodeIso}
  import cats.implicits._

  def apply[F[_]: Async: Logger](session: Session[F]): EmployeeRepository[F] =
    new EmployeeRepository[F] {

      override def findAllEmployees: F[List[Employee]] = {

        val empQuery: Query[Void, Employee] =
          sql"""
              SELECT e.id, e.code, e.name, e.age, e.salary, a.id, a.street, a.city, a.state, a.zip 
              FROM EMPLOYEE e, ADDRESS a
              WHERE e.address = a.id
            """.query(dbToEmployeeDecoder)

        session
          .execute(empQuery)
          .onError(logAndRaise)
      }

      override def findById(id: Long): F[Option[Employee]] = {
        import skunk.codec.all._

        val empQueryById: Query[Long, Employee] =
          sql"""
             SELECT e.id, e.name, e.age, e.salary, a.id, a.street, a.city, a.state, a.zip
             FROM EMPLOYEE e, ADDRESS a
             WHERE e.id = $int8 AND e.address = a.id
           """.query(dbToEmployeeDecoder)

        session
          .prepare(empQueryById)
          .use(_.option(id))
          .onError(logAndRaise)
      }

      override def addEmployee(employee: CreateEmployee): F[EmployeeId] = {

        val addEmployeeRes = for {
          totalEmployees <- fetchTotalEmployeeHandler()
          addressId      <- addAddressHandler(employee.address)
          employeeId     <- addEmployeeHandler(employee, totalEmployees, addressId)
        } yield employeeId

        addEmployeeRes
          .use(_.pure[F])
          .onError(logAndRaise)

      }

      private def addEmployeeHandler(employee: CreateEmployee, employeeId: EmployeeId, addressId: AddressId): Resource[F, EmployeeId] = {
        val addEmployee: Command[EmployeeId ~ EmployeeCode ~ String ~ Int ~ Double ~ AddressId] =
          sql"""
            INSERT INTO EMPLOYEE (ID, CODE,NAME, AGE, SALARY, ADDRESS)
            VALUES (
                ${employeeIdCodec.asEncoder}, 
                ${employeeCodeCodec.asEncoder},
                $varchar,
                $int4,
                $float8,
                ${addressIdCodec.asEncoder}
              )
            """.command

        session
          .prepare(addEmployee)
          .evalMap { cmd =>
            ID
              .make[F, EmployeeCode]
              .flatMap { code =>
                cmd.execute(employeeId ~ code ~ employee.name ~ employee.age ~ employee.salary ~ addressId).as(employeeId)
              }
              .onError(logAndRaise)
          }

      }

      private def fetchTotalEmployeeHandler(): Resource[F, EmployeeId] = {
        val employeeCount: Query[Void, Int] =
          sql"""SELECT ID FROM EMPLOYEE""".query(int4)

        Resource.eval(
          session
            .execute(employeeCount)
            .map(_.sorted.last + 1)
            .map(EmployeeId)
            .onError(logAndRaise)
        )

      }

      private def addAddressHandler(address: CreateAddress): Resource[F, AddressId] = {

        val insertAddressQuery: Command[AddressId ~ CreateAddress] =
          sql"""
              INSERT INTO ADDRESS (ID, STREET, CITY, STATE, ZIP)
              VALUES (${addressIdCodec.asEncoder}, $text, $text, $text, $text)          
             """.command.contramap { case id ~ i =>
            id ~ i.street ~ i.city ~ i.state ~ i.zip
          }

        session
          .prepare(insertAddressQuery)
          .evalMap { cmd =>
            ID
              .make[F, AddressId]
              .flatMap { id =>
                cmd.execute(id ~ address).as(id)
              }
              .onError(logAndRaise)
          }
      }

    }

}
