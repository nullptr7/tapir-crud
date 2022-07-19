package com.github.nullptr7
package protocol

import org.specs2.mutable.SpecificationLike

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
    lazy val allEmployees: List[Employee] = List(
      Employee(
        id      = 1,
        name    = "Paul",
        age     = 32,
        salary  = 20000.0,
        address = Address(123, "Some Street Name", "Some City", "Some State", "123456")
      )
    )
    resp.body must beRight(allEmployees)
  }

  "Address Service" should {
    "work by ID" in withResource { rs =>
      lazy val addressByIdEndpointStub =
        TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
          .whenServerEndpoint(rs.addressByIdEndpoint)
          .thenRunLogic()
          .backend()
      basicRequest
        .get(uri"http://localhost:8080/employees/address?id=123")
        .header("X-AuthMode", "admin")
        .response(asJson[Option[Address]])
        .send(addressByIdEndpointStub)

    }.map {
      _.body must beRight(
        Some(
          Address(
            123,
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
        .get(uri"http://localhost:8080/employees/address?pincode=123456")
        .header("X-AuthMode", "admin")
        .response(asJson[Option[Address]])
        .send(addressByZipEndpointStub)

    }.map {
      _.body must beRight(
        Some(
          Address(
            123,
            "Some Street Name",
            "Some City",
            "Some State",
            "123456"
          )
        )
      )
    }
  }
}
