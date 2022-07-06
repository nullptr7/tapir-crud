package com.github.nullptr7
package protocol

import cats.effect.Async

class ServiceLogic[F[_]: Async] extends Contracts[F] {

  import data._
  import models._
  import cats.implicits._

  private[this] lazy val allEmployeeEndpoint = allEmployeesEP.serverLogic[F] { _ =>
    Right(allEmployees).withLeft[String].pure[F]
  }

  private[this] lazy val empByIdEndpoint = employeeEP.serverLogic[F] { case (authMode, id) =>
    authMode match {
      case "admin" => Right(allEmployees.find(_.id == id.toLong)).withLeft[String].pure[F]
      case _       => Left("Unauthorized").withRight[Option[Employee]].pure[F]
    }
  }

  override val make = List(allEmployeeEndpoint, empByIdEndpoint)
}
