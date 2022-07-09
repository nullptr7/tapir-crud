package com.github.nullptr7
package protocol

import cats.effect.Async
import sttp.model.StatusCode._

class ServiceLogic[F[_]: Async] extends EmployeeContracts[F] with AddressContracts[F] {

  import data._
  import models._
  import cats.implicits._
  import exceptions.ErrorResponse

  private[protocol] lazy val allEmployeeEndpoint = allEmployeesEP.serverLogic[F] {
    case Admin       => Right(allEmployees).withLeft[ErrorResponse].pure[F]
    case NonAdmin    => Left(ErrorResponse(Unauthorized.code, "Not allowed")).withRight[List[Employee]].pure[F]
    case InvalidMode => Left(ErrorResponse(BadRequest.code, "Invalid Mode")).withRight[List[Employee]].pure[F]
  }

  private[protocol] lazy val empByIdEndpoint = employeeEP.serverLogic[F] { case (authMode, id) =>
    authMode match {
      case Admin       => Right(allEmployees.find(_.id == id.toLong)).withLeft[ErrorResponse].pure[F]
      case NonAdmin    => Left(ErrorResponse(Unauthorized.code, "Not allowed")).withRight[Option[Employee]].pure[F]
      case InvalidMode => Left(ErrorResponse(BadRequest.code, "Invalid Mode")).withRight[Option[Employee]].pure[F]
    }
  }

  private[protocol] lazy val addressByIdEndpoint = addressById.serverLogic[F] { case (authMode, id) =>
    authMode match {
      case Admin       => Right(allAddresses.find(_.id == id.toLong)).withLeft[String].pure[F]
      case NonAdmin    => Left("Unauthorized").withRight[Option[Address]].pure[F]
      case InvalidMode => Left("Invalid mode").withRight[Option[Address]].pure[F]
    }
  }

  private[protocol] lazy val addressByZipEndpoint = addressByPincode.serverLogic[F] { case (authMode, pincode) =>
    authMode match {
      case Admin       => Right(allAddresses.find(_.zip == pincode)).withLeft[String].pure[F]
      case NonAdmin    => Left("Unauthorized").withRight[Option[Address]].pure[F]
      case InvalidMode => Left("Invalid mode").withRight[Option[Address]].pure[F]
    }
  }

  // private[this] def handleBadAuth[O](authMode: AuthMode)(msg: => String): F[Either[String, O]] = authMode match {
  //   case NonAdmin | InvalidMode => Left(msg).withRight[O].pure[F]
  // }

  override val make = List(allEmployeeEndpoint, empByIdEndpoint)
}
