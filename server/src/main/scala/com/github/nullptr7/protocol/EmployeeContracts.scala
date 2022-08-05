package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import models._
import models.codecs._
import exceptions._
import ErrorResponse._

trait EmployeeContracts[F[_]] extends Contracts[F] {

  protected[protocol] lazy val allEmployeesEP: Endpoint[Unit, AuthMode, ServiceResponseException, List[Employee], Any] =
    base
      .get
      .in("get" / "all")
      .out(jsonBody[List[Employee]])
      .errorOut(jsonBody[ServiceResponseException])

  protected[protocol] lazy val employeeEP: Endpoint[Unit, (AuthMode, String), ServiceResponseException, Option[Employee], Any] =
    base
      .get
      .in("get" / "employee")
      .in(query[String]("id"))
      .out(jsonBody[Option[Employee]])
      .errorOut(jsonBody[ServiceResponseException])

//      .errorOut(
//        oneOf[ServiceResponseException](
//          oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[GenericException])),
//          oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[ServiceResponseException])),
//          oneOfVariant(statusCode(StatusCode.NonAuthoritativeInformation).and(jsonBody[InvalidAuthException.type]))
//        )
//      )

  protected[protocol] lazy val addEmployeeEP =
    base
      .post
      .in("add" / "employee")
      .in(jsonBody[CreateEmployee])
      .out(jsonBody[EmployeeId])
      .errorOut(jsonBody[ServiceResponseException])

}
