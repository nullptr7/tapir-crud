package com.github.nullptr7
package protocol

import java.util.UUID

import org.mockito.MockitoSugar.when
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global

import sttp.client3._
import sttp.client3.circe._
import sttp.client3.impl.cats.implicits._
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.tapir.server.stub.TapirStubInterpreter

import io.circe.parser._
import io.circe.syntax.EncoderOps

import models.codecs.{addressCodec, addressIdCodec, createAddressCodec}
import models.{Address, AddressId, CreateAddress}
import exceptions.ErrorResponse._
import common.BaseTest
import mocks.data._

class AddressServiceLogicTest extends BaseTest with ServiceLogicTestHelper[IO] {

  import serviceLogic._

  implicit override protected def async:  Async[IO]  = IO.asyncForIO
  implicit override protected def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

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

  private lazy val addAddressEndpointStub =
    TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(addAddressEndpoint)
      .thenRunLogic()
      .backend()

  "Address Service By ID endpoint" should "work when admin and valid id" in {

    when(serviceLogic.addressRepo.findAddressById(addressId))
      .thenReturn(IO.pure(allAddresses.find(_.id == addressId)))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressById?id=${addressId.value.toString}")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByIdEndpointStub)

    response.unsafeRunSync().body shouldBe Right(
      Some(
        Address(
          id     = addressId,
          street = "Main Street",
          city   = "Anytown",
          state  = "CA",
          zip    = "12345"
        )
      )
    )
  }

  it should "return empty response when admin and incorrect id" in {

    when(serviceLogic.addressRepo.findAddressById(addressId))
      .thenReturn(IO.pure(Option.empty[Address]))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressById?id=${addressId.value.toString}")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByIdEndpointStub)

    response.unsafeRunSync().body shouldBe Right(None)
  }

  it should "return unauthorized when AuthMode is nonadmin" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressById?id=777")
      .header("X-AuthMode", "nonadmin")
      .send(addressByIdEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }

  it should "return auth header not passed exception when AuthMode is not passed in the header" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressById?id=777")
      .send(addressByIdEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe MissingAuthException
      }
    }
  }

  "Address Service By Zip endpoint" should "work when admin and valid zip" in {

    when(serviceLogic.addressRepo.findAddressByZip("12345"))
      .thenReturn(IO.pure(allAddresses.find(_.zip.toInt == 12345)))

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressByZip?pincode=12345")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByZipEndpointStub)

    response.unsafeRunSync().body shouldBe Right(
      Some(
        Address(
          id     = addressId,
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
      .get(uri"http://localhost:8080/employees/addressByZip?pincode=963963963")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Address]])
      .send(addressByZipEndpointStub)

    response.unsafeRunSync().body shouldBe Right(None)
  }

  it should "return unauthorized when nonadmin" in {
    val response = basicRequest
      .get(uri"http://localhost:8080/employees/addressByZip?pincode=963963963")
      .header("X-AuthMode", "nonadmin")
      .send(addressByZipEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }

  "Add Address Service endpoint" should "successfully add the address" in {

    val givenUUID        = UUID.randomUUID()
    val addressInRequest = CreateAddress("street", "city", "state", "123456")

    when(serviceLogic.addressRepo.addAddress(addressInRequest))
      .thenReturn(IO.pure(AddressId(givenUUID)))

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/address")
      .body(addressInRequest.asJson.toString)
      .header("X-AuthMode", "admin")
      .response(asJson[AddressId])
      .send(addAddressEndpointStub)

    response.unsafeRunSync().body shouldBe Right(AddressId(givenUUID))
  }

  it should "return unauthorized when AuthMode is nonadmin" in {

    val addressInRequest = CreateAddress("street", "city", "state", "123456")

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/address")
      .body(addressInRequest.asJson.toString)
      .header("X-AuthMode", "nonadmin")
      .send(addAddressEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }

  it should "return invalid authentication when AuthMode header is not passed correctly" in {
    val addressInRequest = CreateAddress("street", "city", "state", "123456")

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/address")
      .body(addressInRequest.asJson.toString)
      .header("X-AuthMode", "xxx")
      .send(addAddressEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe InvalidAuthException
      }
    }
  }

  it should "return auth header not passed exception when AuthMode header is not passed" in {

    val addressInRequest = CreateAddress("street", "city", "state", "123456")

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/address")
      .body(addressInRequest.asJson.toString)
      .send(addAddressEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe MissingAuthException
      }
    }

  }
}
