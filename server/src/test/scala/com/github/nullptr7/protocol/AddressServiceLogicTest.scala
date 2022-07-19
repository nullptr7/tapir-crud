package com.github.nullptr7
package protocol

import org.mockito.MockitoSugar.when

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import sttp.client3._
import sttp.client3.circe._
import sttp.client3.impl.cats.implicits._
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.tapir.server.stub.TapirStubInterpreter

import io.circe.parser._

import models.codecs.addressCodec
import models.Address
import exceptions.ErrorResponse._
import common.BaseTest
import mocks.data._

class AddressServiceLogicTest extends BaseTest with ServiceLogicTestHelper {

  import serviceLogic._

  private lazy val addressByIdEndpointStub =
    TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(addressByIdEndpoint)
      .thenRunLogic()
      .backend()

  private lazy val addressByZipEndpointStub =
    TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(addressByZipEndpoint)
      .thenRunLogic()
      .backend()

  "Address Service By ID endpoint" should "work when admin and valid id" in {

    when(serviceLogic.addressRepo.findAddressById(123))
      .thenReturn(IO.pure(allAddresses.find(_.id == 123)))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?id=123")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByIdEndpointStub)

    response.unsafeRunSync().body shouldBe Right(
      Some(
        Address(
          id     = 123,
          street = "Main Street",
          city   = "Anytown",
          state  = "CA",
          zip    = "12345"
        )
      )
    )
  }

  it should "return empty response when admin and incorrect id" in {

    when(serviceLogic.addressRepo.findAddressById(777))
      .thenReturn(IO.pure(Option.empty[Address]))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?id=777")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByIdEndpointStub)

    response.unsafeRunSync().body shouldBe Right(None)
  }

  it should "return unauthorized when AuthMode is nonadmin" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?id=777")
      .header("X-AuthMode", "nonadmin")
      .send(addressByIdEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }

  "Address Service By Zip endpoint" should "work when admin and valid zip" in {

    when(serviceLogic.addressRepo.findAddressByZip("12345"))
      .thenReturn(IO.pure(allAddresses.find(_.zip.toInt == 12345)))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?pincode=12345")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByZipEndpointStub)

    response.unsafeRunSync().body shouldBe Right(
      Some(
        Address(
          id     = 123,
          street = "Main Street",
          city   = "Anytown",
          state  = "CA",
          zip    = "12345"
        )
      )
    )
  }

  it should "return empty response when admin and incorrect zip" in {

    when(serviceLogic.addressRepo.findAddressByZip("963963963"))
      .thenReturn(IO.pure(Option.empty[Address]))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?pincode=963963963")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByZipEndpointStub)

    response.unsafeRunSync().body shouldBe Right(None)
  }

  it should "return unauthorized when nonadmin" in {
    val response = basicRequest
      .get(uri"http://localhost:8080/employees/address?pincode=963963963")
      .header("X-AuthMode", "nonadmin")
      .send(addressByZipEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }
}
