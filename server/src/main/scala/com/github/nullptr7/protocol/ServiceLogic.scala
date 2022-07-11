package com.github.nullptr7
package protocol

import cats.effect.Async

import exceptions.ErrorResponse.{InvalidAuthException, UnauthorizedAuthException}

class ServiceLogic[F[_]: Async] extends EmployeeContracts[F] with AddressContracts[F] {

  import data._
  import cats.implicits._

  private[protocol] lazy val allEmployeeEndpoint = allEmployeesEP.serverLogicRecoverErrors[F](handle(_)(allEmployees.pure[F]))

  private[protocol] lazy val empByIdEndpoint = employeeEP.serverLogicRecoverErrors[F] { case (authMode, id) =>
    handle(authMode)(allEmployees.find(_.id == id.toLong).pure[F])
  }

  private[protocol] lazy val addressByIdEndpoint = addressById.serverLogicRecoverErrors[F] { case (authMode, id) =>
    handle(authMode)(allAddresses.find(_.id == id.toLong).pure[F])
  }

  private[protocol] lazy val addressByZipEndpoint = addressByPincode.serverLogicRecoverErrors[F] { case (authMode, pincode) =>
    handle(authMode)(allAddresses.find(_.zip == pincode).pure[F])
  }

  override val make = List(allEmployeeEndpoint, empByIdEndpoint)

  private[this] def handle[O](authMode: AuthMode)(fo: => F[O]): F[O] = authMode match {
    case Admin       => fo
    case NonAdmin    => Async[F].raiseError[O](UnauthorizedAuthException)
    case InvalidMode => Async[F].raiseError[O](InvalidAuthException)
  }

}
