package com.github.nullptr7
package entrypoint
package modules

import java.util.UUID

import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.typelevel.log4cats.Logger

import cats.effect.Resource
import cats.effect.kernel.Async

import io.circe.syntax.EncoderOps

import client.ApiClients
import exceptions.ErrorResponse._
import models.{AddressId, EmployeeWithTransport, TransportRequest, TransportResponse}
import protocol._

final class ServiceLogicModule[F[_]: Async: Logger](val repositoryModule: RepositoryModule[F], val clients: ApiClients[F])
  extends EmployeeContracts[F]
  with AddressContracts[F] {

  import cats.implicits._
  import clients._

  private def logAuthModeRequest(authMode: AuthMode): F[Unit] = Logger[F].info(s"Requested with auth mode as ${authMode.toString}")

  lazy val addEmployeeEndpoint: ServerEndpointF =
    addEmployeeEP.serverLogicRecoverErrors[F] { case (authMode, employeeBody) =>
      handle(authMode)(repositoryModule.addEmployee(employeeBody))
    }

  lazy val allEmployeeEndpoint: ServerEndpointF =
    allEmployeesEP.serverLogicRecoverErrors[F](handle(_) {

      for {
        a <- repositoryModule.findAllEmployees
        b <- a.traverse { emp =>
               import models.codecs._
               Logger[F].info(s"sending request ${TransportRequest(emp.code).asJson.toString}") *>
                 Logger[F].info(s"emp - $emp") *>
                 transportServiceClient
                   .sendAndReceive[TransportRequest, TransportResponse](Some(TransportRequest(emp.code)))
                   .handleErrorWith { t: Throwable =>
                     Logger[F].warn(t)("Error Received from the server") *>
                       TransportResponse.apply(emp.code).pure[F]
                   }
                   .map(EmployeeWithTransport.apply(emp, _))
             }
      } yield b
    })

  lazy val empByIdEndpoint: ServerEndpointF =
    employeeEP.serverLogicRecoverErrors[F] { case (authMode, id) =>
      handle(authMode)(repositoryModule.findEmployeeById(id.toLong))
    }

  lazy val addressByIdEndpoint: ServerEndpointF =
    addressById.serverLogicRecoverErrors[F] { case (authMode, id) =>
      handle(authMode)(repositoryModule.findAddressById(AddressId(UUID.fromString(id))))
    }

  lazy val addressByZipEndpoint: ServerEndpointF =
    addressByPincode.serverLogicRecoverErrors[F] { case (authMode, pincode) =>
      handle(authMode)(repositoryModule.findAddressByZip(pincode))
    }

  lazy val addAddressEndpoint: ServerEndpointF =
    addAddress.serverLogicRecoverErrors[F] { case (authMode, address) =>
      handle(authMode)(repositoryModule.addAddress(address))
    }

  override val make: Resource[F, List[ServerEndpointF]] =
    Resource
      .pure(
        List(
          allEmployeeEndpoint,
          empByIdEndpoint,
          addressByIdEndpoint,
          addressByZipEndpoint,
          addAddressEndpoint,
          addEmployeeEndpoint
        )
      )
      .evalTap(_ => Logger[F].info("Loading endpoints..."))

  private[this] def handle[O](authMode: AuthMode)(fo: => F[O]): F[O] =
    logAuthModeRequest(authMode) *> (authMode match {
      case Admin           =>
        fo.attemptTap {
          case Left(t)      => Logger[F].error(t.getMessage) *> GenericException("Internal Server Error").raiseError[F, O]
          case Right(value) => value.pure[F]
        }
      case MissingAuthMode => MissingAuthException.raiseError[F, O]
      case NonAdmin        => UnauthorizedAuthException.raiseError[F, O]
      case InvalidMode     => InvalidAuthException.raiseError[F, O]
    })

}
