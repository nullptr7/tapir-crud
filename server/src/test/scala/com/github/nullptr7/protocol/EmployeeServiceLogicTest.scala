package com.github.nullptr7
package protocol

import java.net.URI

import org.http4s
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}

import sttp.client3._
import sttp.client3.circe._
import sttp.client3.impl.cats.implicits._
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadAsyncError
import sttp.tapir.server.stub.TapirStubInterpreter

import io.circe.parser._
import io.circe.syntax.EncoderOps

import client.TransportServiceClient
import configurations.types.{Sensitive, TransportApiClientDetails}
import common.BaseTest
import exceptions.ErrorResponse._
import mocks.data._
import models.codecs._

class EmployeeServiceLogicTest extends BaseTest with ServiceLogicTestHelper[IO] {

  import models._
  import serverLogicModule._

  implicit override protected def logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit override protected def async:  Async[IO]  = IO.asyncForIO

  private lazy val allEmployeeEndpointStub =
    TapirStubInterpreter(SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(allEmployeeEndpoint)
      .thenRunLogic()
      .backend()

  private lazy val employeeByIdEndpointStub =
    TapirStubInterpreter[IO, Either[String, Option[Employee]]](SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(empByIdEndpoint)
      .thenRunLogic()
      .backend()

  private lazy val addEmployeeEndpointStub =
    TapirStubInterpreter[IO, Either[String, EmployeeCode]](SttpBackendStub(implicitly[MonadAsyncError[IO]]))
      .whenServerEndpoint(addEmployeeEndpoint)
      .thenRunLogic()
      .backend()

  "All Employee Endpoint with authMode header" should "work when admin" in {

    when(serverLogicModule.repositoryModule.findAllEmployees)
      .thenReturn(IO.pure(allEmployees))

    import models.DAY

    val client = mock[Client[IO]]

    when(client.run(any[http4s.Request[IO]]))
      .thenReturn(
        Resource.pure[IO, http4s.Response[IO]](
          http4s
            .Response[IO](
              Status.Ok
            )
            .withEntity(
              TransportResponse(employeeCode1, 1, 1, DAY)
            )
        )
      )

    when(serverLogicModule.clients.transportServiceClient)
      .thenReturn(
        new TransportServiceClient[IO](
          client,
          TransportApiClientDetails(
            URI.create("https://2ff8313d-c02d-4113-9768-501060fa697d.mock.pstmn.io/api/get/employee-transport-data"),
            "scott",
            Sensitive("tiger")
          )
        )
      )

    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/all")
      .header("X-AuthMode", "admin")
      .response(asJson[List[EmployeeWithTransport]])
      .send(allEmployeeEndpointStub)

    response.unsafeRunSync().body shouldBe Right(
      List(
        EmployeeWithTransport(
          id               = employeeId1,
          code             = employeeCode1,
          name             = "John",
          age              = 12,
          salary           = 1000,
          address          = Address(addressId, "Main Street", "Anytown", "CA", "12345"),
          transportDetails = TransportResponse(employeeCode1, 1, 1, DAY)
        )
      )
    )
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

    when(serverLogicModule.repositoryModule.findEmployeeById(1))
      .thenReturn(IO.pure(allEmployees.find(_.id == employeeId1)))
    // when
    val response = basicRequest
      .get(uri"http://localhost:8080/employees/get/employee?id=1")
      .header("X-AuthMode", "admin")
      .response(asJson[Option[Employee]])
      .send(employeeByIdEndpointStub)

    // then
    response.unsafeRunSync().body shouldBe Right(
      Some(
        Employee(employeeId1, employeeCode1, "John", 12, 1000.0, Address(addressId, "Main Street", "Anytown", "CA", "12345"))
      )
    )

  }

  it should "return none when the authMode is admin but id is not available from the list" in {

    when(serverLogicModule.repositoryModule.findEmployeeById(9))
      .thenReturn(IO.pure(Option.empty[Employee]))

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

  "Add Employee Endpoint with authMode header" should "work when admin and valid payload" in {

    val scott = CreateEmployee("Scott", 12, 10.0, CreateAddress("Street1", "City1", "State1", "123456"))

    when(
      serverLogicModule
        .repositoryModule
        .addEmployee(scott)
    )
      .thenReturn(IO.pure(EmployeeId(1)))

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/add/employee")
      .body(scott.asJson.toString)
      .header("X-AuthMode", "admin")
      .response(asJson[EmployeeId])
      .send(addEmployeeEndpointStub)

    response.unsafeRunSync().body shouldBe Right(EmployeeId(1))
  }

  it should "fail when authMode header is nonadmin" in {

    val badScott =
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

    val response = basicRequest
      .post(uri"http://localhost:8080/employees/add/employee")
      .body(badScott)
      .header("X-AuthMode", "nonadmin")
      .send(addEmployeeEndpointStub)

    val errorBody = response.unsafeRunSync().body

    inside(errorBody) { case Left(value) =>
      inside(decode[ServiceResponseException](value)) { case Right(value) =>
        value shouldBe UnauthorizedAuthException
      }
    }
  }
}
