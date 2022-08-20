package com.github.nullptr7
package client

import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global

import sttp.tapir._
import sttp.tapir.client.http4s.Http4sClientInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

import io.circe.generic.auto.{exportDecoder, exportEncoder}

object TimePass extends App {

  private val value: EndpointInput[Unit] = "api" / "get" / "employee-transport-data"

  case class Something(code: String)

  private val userEndpoint: Endpoint[Unit, Something, Unit, String, Any] =
    endpoint
      .post
      .in(value)
      .in(header("x-mock-match-request-body", "true"))
      .in(jsonBody[Something])
      .out(stringBody)

  val (req, resp) =
    Http4sClientInterpreter[IO]()
      .toRequest(userEndpoint, Some(Uri.unsafeFromString("https://2ff8313d-c02d-4113-9768-501060fa697d.mock.pstmn.io")))
      .apply(Something("4a5f132a-084b-445f-b0b0-3e1f1f36521c"))

  val client: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].resource

  println(
    client
      .use(
        _.run(req)
          .use(resp)
      )
      .unsafeRunSync()
  )

}
