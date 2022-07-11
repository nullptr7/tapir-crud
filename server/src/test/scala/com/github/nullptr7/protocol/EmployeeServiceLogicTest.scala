package com.github.nullptr7
package protocol

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.parser._
import io.circe.syntax._
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.impl.cats.implicits._
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.tapir.server.stub.TapirStubInterpreter

import data._
import models.codecs._
import exceptions.ErrorResponse._
import common.BaseTest

class EmployeeServiceLogicTest extends BaseTest with ServiceLogicTestHelper {

  import serviceLogic._

  private lazy val allEmployeeEndpointStub =
    TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(allEmployeeEndpoint)
      .thenRunLogic()
      .backend()

  import models._

  private lazy val employeeByIdEndpointStub =
    TapirStubInterpreter[IO, Either[String, Option[Employee]]](SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(empByIdEndpoint)
      .thenRunLogic()
      .backend()

  "All Employee Endpoint with authMode header" should "work when admin" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/all")
      .header("X-AuthMode", "admin")
      .send(allEmployeeEndpointStub)

    response.unsafeRunSync().body shouldBe Right(allEmployees.asJson.noSpaces)
  }

  it should "fail when not admin" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/all")
      .header("X-AuthMode", "nonadmin")
      .send(allEmployeeEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }

  }

  it should "fail when header format is not correct" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/all")
      .header("X-AuthMode", "xxx")
      .send(allEmployeeEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe InvalidAuthException
      }
    }

  }

  "EmployeeById endpoint with authMode header" should "work when admin and valid id" in {

    // when
    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/employee?id=1")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Employee]])
      .send(employeeByIdEndpointStub)

    // then
    response.unsafeRunSync().body shouldBe Right(
      Some(
        Employee(1, "John", 12, 1000.0, Address(123, "Main Street", "Anytown", "CA", "12345"))
      )
    )

  }

  it should "return none when the authMode is admin but id is not available from the list" in {

    // when
    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/employee?id=9")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Employee]])
      .send(employeeByIdEndpointStub)

    // then
    response.unsafeRunSync().body shouldBe Right(None)
  }

  it should "return unauthorized when authMode is non-admin" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/employee?id=1")
      .header("X-AuthMode", "nonadmin")
      .send(employeeByIdEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }

  it should "fail when header format is not correct" in {

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/employee?id=1")
      .header("X-AuthMode", "xxx")
      .send(employeeByIdEndpointStub)

    val errorBody = response.unsafeRunSync().body
    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe InvalidAuthException
      }
    }
  }
}
