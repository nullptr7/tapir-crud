package com.github.nullptr7
package protocol

import java.util.UUID

import org.specs2.mutable.SpecificationLike
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect._
import cats.effect.testing.specs2.{CatsEffect, CatsResource}

import sttp.client3._
import sttp.client3.circe._
import sttp.client3.impl.cats.implicits._
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.tapir.server.stub.TapirStubInterpreter

import fs2.io.net.Network

import helper.PostgresSessionHelper
import models._
import models.codecs._
import storage._

class IntegrationSuite extends CatsResource[IO, ServiceLogic[IO]] with SpecificationLike with CatsEffect with PostgresSessionHelper[IO] {
  sequential
  implicit override val concurrent: Concurrent[IO]  = IO.asyncForIO
  implicit override val console:    std.Console[IO] = IO.consoleForIO
  implicit override val network:    Network[IO]     = Network.forAsync[IO]

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val employeeId1:   EmployeeId   = EmployeeId(1)
  private val employeeCode1: EmployeeCode = EmployeeCode(UUID.fromString("75b107cb-2bef-431f-8b33-b2074d51bd08"))
  private val addressId:     AddressId    = AddressId(UUID.fromString("20d88c49-01e9-40d0-b568-982100e676ba"))

  override val resource: Resource[IO, ServiceLogic[IO]] =
    for {
      session      <- sessionR
      serviceLogic <- Resource.pure[IO, ServiceLogic[IO]](
                        new ServiceLogic[IO](
                          EmployeeRepository.apply[IO](session),
                          AddressRepository.apply[IO](session)
                        )
                      )
    } yield serviceLogic

  "Add New Employee should work" in withResource { rs =>
    lazy val scott =
      """
        |{
        | "name": "Scott",
        | "age": 12,
        | "salary": 10.0,
        | "address": {
        |   "street": "Street1",
        |   "city": "City1",
        |   "state": "State1",
        |   "zip": "1234567"
        | }
        |}
        |""".stripMargin

    lazy val addEmployeeEndpointStub =
      TapirStubInterpreter[IO, Either[String, EmployeeCode]](SttpBackendStub(implicitly[MonadAsyncError[IO]]))
        .whenServerEndpoint(rs.addEmployeeEndpoint)
        .thenRunLogic()
        .backend()

    basicRequest
      .post(uri"http://localhost:8080/employees/add/employee")
      .body(scott)
      .header("X-AuthMode", "admin")
      .response(asJson[EmployeeId])
      .send(addEmployeeEndpointStub)
  }.map {
    _.body must beRight
  }

  "All Employee Endpoint should work" in withResource { serviceLogic =>
    lazy val allEmployeeEndpointStub =
      TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
        .whenServerEndpoint(serviceLogic.allEmployeeEndpoint)
        .thenRunLogic()
        .backend()

    basicRequest
      .get(uri"http://localhost:8080/employees/get/all")
      .header("X-AuthMode", "admin")
      .response(asJson[List[Employee]])
      .send(allEmployeeEndpointStub)

  }.map { resp =>
    lazy val paul =
      Employee(
        id      = employeeId1,
        code    = employeeCode1,
        name    = "Paul",
        age     = 32,
        salary  = 20000.0,
        address = Address(addressId, "Some Street Name", "Some City", "Some State", "123456")
      )

    resp.body must beRight
    resp.body.toOption.get should contain(paul)
  }

  "Address Service" should {
    "work by ID" in withResource { rs =>
      lazy val addressByIdEndpointStub =
        TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
          .whenServerEndpoint(rs.addressByIdEndpoint)
          .thenRunLogic()
          .backend()
      basicRequest
        .get(uri"http://localhost:8080/employees/addressById?id=${addressId.value.toString}")
        .header("X-AuthMode", "admin")
        .response(asJson[Option[Address]])
        .send(addressByIdEndpointStub)

    }.map {
      _.body must beRight(
        Some(
          Address(
            addressId,
            "Some Street Name",
            "Some City",
            "Some State",
            "123456"
          )
        )
      )
    }

    "work by Zip" in withResource { rs =>
      lazy val addressByZipEndpointStub =
        TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
          .whenServerEndpoint(rs.addressByZipEndpoint)
          .thenRunLogic()
          .backend()

      basicRequest
        .get(uri"http://localhost:8080/employees/addressByZip?pincode=123456")
        .header("X-AuthMode", "admin")
        .response(asJson[Option[Address]])
        .send(addressByZipEndpointStub)

    }.map {
      _.body must beRight(
        Some(
          Address(
            addressId,
            "Some Street Name",
            "Some City",
            "Some State",
            "123456"
          )
        )
      )
    }

    "add new address successfully" in withResource { rs =>
      lazy val goodRequest =
        """
          {
            "street": "Some Awesome Street Address",
            "city": "Some City",
            "state": "Some State",
            "zip": "123456"
          }
        """.stripMargin

      lazy val addAddressEndpointStub =
        TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
          .whenServerEndpoint(rs.addAddressEndpoint)
          .thenRunLogic()
          .backend()

      basicRequest
        .post(uri"http://localhost:8080/employees/address")
        .body(goodRequest)
        .header("X-AuthMode", "admin")
        .response(asJson[AddressId])
        .send(addAddressEndpointStub)
    }.map {
      _.body must beRight
    }
  }
}
