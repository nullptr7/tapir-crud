package com.github.nullptr7
package protocol

import java.util.UUID

import org.typelevel.log4cats.Logger

import cats.effect.{Async, Resource}

import exceptions.ErrorResponse._
import storage.{AddressRepository, EmployeeRepository}
import models.AddressId

class ServiceLogic[F[_]: Async: Logger](
  private[protocol] val employeeRepo: EmployeeRepository[F],
  private[protocol] val addressRepo:  AddressRepository[F]
) extends EmployeeContracts[F]
  with AddressContracts[F] {

  import cats.implicits._

  private def logAuthModeRequest(authMode: AuthMode): F[Unit] = Logger[F].info(s"Requested with auth mode as ${authMode.toString}")

  private[protocol] lazy val addEmployeeEndpoint: ServerEndpointF =
    addEmployeeEP.serverLogicRecoverErrors[F] { case (authMode, employeeBody) =>
      handle(authMode)(employeeRepo.addEmployee(employeeBody))
    }

  private[protocol] lazy val allEmployeeEndpoint: ServerEndpointF =
    allEmployeesEP.serverLogicRecoverErrors[F](handle(_)(employeeRepo.findAllEmployees))

  private[protocol] lazy val empByIdEndpoint: ServerEndpointF =
    employeeEP.serverLogicRecoverErrors[F] { case (authMode, id) =>
      handle(authMode)(employeeRepo.findById(id.toLong))
    }

  private[protocol] lazy val addressByIdEndpoint: ServerEndpointF =
    addressById.serverLogicRecoverErrors[F] { case (authMode, id) =>
      handle(authMode)(addressRepo.findAddressById(AddressId(UUID.fromString(id))))
    }

  private[protocol] lazy val addressByZipEndpoint: ServerEndpointF =
    addressByPincode.serverLogicRecoverErrors[F] { case (authMode, pincode) =>
      handle(authMode)(addressRepo.findAddressByZip(pincode))
    }

  private[protocol] lazy val addAddressEndpoint: ServerEndpointF =
    addAddress.serverLogicRecoverErrors[F] { case (authMode, address) =>
      handle(authMode)(addressRepo.addAddress(address))
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
