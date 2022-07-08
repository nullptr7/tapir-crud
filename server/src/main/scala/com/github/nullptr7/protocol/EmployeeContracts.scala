package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import models._
import models.codecs._

trait EmployeeContracts[F[_]] extends Contracts[F] {

  import AuthMode._

  protected[protocol] lazy val allEmployeesEP: Endpoint[Unit, AuthMode, String, List[Employee], Any] =
    base
      .get
      .in("get" / "all")
      .in(header[AuthMode]("X-AuthMode"))
      .out(jsonBody[List[Employee]])
      .errorOut(stringBody)

  protected[protocol] lazy val employeeEP: Endpoint[Unit, (AuthMode, String), String, Option[Employee], Any] =
    base
      .get
      .in("get" / "employee")
      .in(header[AuthMode]("X-AuthMode"))
      .in(query[String]("id"))
      .out(jsonBody[Option[Employee]])
      .errorOut(stringBody)

}
