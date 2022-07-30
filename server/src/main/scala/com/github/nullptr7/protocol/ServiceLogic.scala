package com.github.nullptr7
package protocol

import java.util.UUID

import cats.effect.Async

import exceptions.ErrorResponse._
import storage.{AddressRepository, EmployeeRepository}

class ServiceLogic[F[_]: Async](
  private[protocol] val employeeRepo: EmployeeRepository[F],
  private[protocol] val addressRepo:  AddressRepository[F]
) extends EmployeeContracts[F]
  with AddressContracts[F] {

  import cats.implicits._

  private[protocol] lazy val allEmployeeEndpoint: ServerEndpointF =
    allEmployeesEP.serverLogicRecoverErrors[F](handle(_)(employeeRepo.findAllEmployees))

  private[protocol] lazy val empByIdEndpoint: ServerEndpointF = employeeEP.serverLogicRecoverErrors[F] { case (authMode, id) =>
    handle(authMode)(employeeRepo.findById(id.toLong))
  }

  private[protocol] lazy val addressByIdEndpoint: ServerEndpointF = addressById.serverLogicRecoverErrors[F] { case (authMode, id) =>
    handle(authMode)(addressRepo.findAddressById(UUID.fromString(id)))
  }

  private[protocol] lazy val addressByZipEndpoint: ServerEndpointF = addressByPincode.serverLogicRecoverErrors[F] { case (authMode, pincode) =>
    handle(authMode)(addressRepo.findAddressByZip(pincode))
  }

  private[protocol] lazy val addAddressEndpoint: ServerEndpointF = addAddress.serverLogicRecoverErrors[F] { case (address, authMode) =>
    handle(authMode)(addressRepo.addAddress(address))
  }

  override val make: F[List[ServerEndpointF]] =
    List(
      allEmployeeEndpoint,
      empByIdEndpoint,
      addressByIdEndpoint,
      addressByZipEndpoint,
      addAddressEndpoint
    )
      .pure[F]

  private[this] def handle[O](authMode: AuthMode)(fo: => F[O]): F[O] = authMode match {
    case Admin       => fo.adaptErr { case t: Throwable => GenericException(t.getMessage) }
    case NonAdmin    => Async[F].raiseError[O](UnauthorizedAuthException)
    case InvalidMode => Async[F].raiseError[O](InvalidAuthException)
  }

}
