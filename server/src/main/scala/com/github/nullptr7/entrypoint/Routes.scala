package com.github.nullptr7
package entrypoint

import org.http4s.HttpApp
import org.typelevel.log4cats.Logger

import cats.effect.kernel.{Async, Resource}
import cats.implicits._

import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.statusCode

import io.circe.generic.auto._

import exceptions.ErrorResponse.{ServiceResponseException, GenericException}

object Routes {

  private[entrypoint] def make[F[_]: Async: Logger](serverLogic: List[ServerEndpoint[Any, F]]): Resource[F, HttpApp[F]] = {

    lazy val defaultServerLog =
      Http4sServerOptions
        .defaultServerLog
        .copy(doLogWhenReceived = x => Logger[F].info(x), doLogWhenHandled = (x, _) => Logger[F].info(x))

    /*
      TODO: Need to properly propogate error as, currently only F[O].attempt works whereas others do not.
    */
    Resource.pure {
      Http4sServerInterpreter[F](
        Http4sServerOptions
          .customiseInterceptors[F]
          .serverLog(defaultServerLog)
          .exceptionHandler {
            ExceptionHandler[F](ex =>
              Option
                .apply[ValuedEndpointOutput[_]](
                  ValuedEndpointOutput(
                    jsonBody[ServiceResponseException].and(statusCode(StatusCode.InternalServerError)),
                    GenericException(ex.e.getMessage)
                  )
                )
                .pure[F]
            )
          }
          .options
      ).toRoutes(serverLogic).orNotFound
    }.evalTap(_ => Logger[F].info("Making routes..."))
  }

}
