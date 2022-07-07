package com.github.nullptr7
package protocol

import cats.effect.Async

class ServiceLogic[F[_]: Async] extends Contracts[F] {

  import data._
  import models._
  import cats.implicits._

  private[protocol] lazy val allEmployeeEndpoint = allEmployeesEP.serverLogic[F] {
    case Admin    => Right(allEmployees).withLeft[String].pure[F]
    case NonAdmin => Left("Unauthorized").withRight[List[Employee]].pure[F]
  }

  private[protocol] lazy val empByIdEndpoint = employeeEP.serverLogic[F] { case (authMode, id) =>
    authMode match {
      case Admin    => Right(allEmployees.find(_.id == id.toLong)).withLeft[String].pure[F]
      case NonAdmin => Left("Unauthorized").withRight[Option[Employee]].pure[F]
    }
  }

  override val make = List(allEmployeeEndpoint, empByIdEndpoint)
}
