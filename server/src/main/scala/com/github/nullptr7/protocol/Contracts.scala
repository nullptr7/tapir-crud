package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

import models._
import models.codecs._

trait Contracts[F[_]] {

  protected[protocol] lazy val allEmployeesEP =
    endpoint
      .get
      .in("employees" / "get" / "all")
      .in(header[String]("X-AuthMode"))
      .out(jsonBody[List[Employee]])
      .errorOut(stringBody)

  protected[protocol] lazy val employeeEP =
    endpoint
      .get
      .in("employees" / "get" / "employee")
      .in(header[String]("X-AuthMode"))
      .in(query[String]("id"))
      .out(jsonBody[Option[Employee]])
      .errorOut(stringBody)

  protected[protocol] val make: List[ServerEndpoint[Any, F]]

}
